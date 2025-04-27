package com.example;

import com.prowidesoftware.swift.io.parser.SwiftParser;
import com.prowidesoftware.swift.model.SwiftMessage;
import com.prowidesoftware.swift.model.field.*;
import com.prowidesoftware.swift.model.mt.mt9xx.MT940;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import std.iso20022.xsd.camt_052_001_13.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

public class MT940ToCamt052Converter {

    public static void main(String[] args) throws Exception {
        String mt940 = "{1:F01YOURBANKXXXX0000000000}{2:I940DESTBANKXXXXN}{4:\n" +
                ":20:ABC123456789\n" +
                ":21:REF456\n" +
                ":25:OM123456789\n" +
                ":28C:123/1\n" +
                ":60F:C180101OMR12345,67\n" +
                ":61:1801020102C1000,00NTRFNONREF//1234567890\n" +
                ":86:/TRCD/Transfer 1\n" +
                ":61:1801030103D200,00NCHGREF2//2345678901\n" +
                ":86:/TRCD/Transfer 2\n" +
                ":62F:C180104OMR13445,67\n" +
                ":64:C180104OMR13345,67\n" +
                "-}";

        String camt052 = new MT940ToCamt052Converter().convertMT940ToCamt053(mt940);
        System.out.println(camt052);
    }

    public String convertMT940ToCamt053(String mt940Text) throws Exception {
        SwiftParser parser = new SwiftParser(new StringReader(mt940Text));
        SwiftMessage swiftMessage = parser.message();
        MT940 mt940 = new MT940(swiftMessage);

        ObjectFactory factory = new ObjectFactory();
        Document document = factory.createDocument();
        BankToCustomerStatementV13 bkToCtmrStmt13 = factory.createBankToCustomerStatementV13();
        document.setBkToCstmrStmt(bkToCtmrStmt13);

        // :20: Transaction Reference Number -> GrpHdr.MsgId and Rpt.Id
        GroupHeader116 groupHeader = factory.createGroupHeader116();
        String msgId = mt940.getField20() != null ? mt940.getField20().getValue() : "MSG-" + UUID.randomUUID();
        groupHeader.setMsgId(msgId);
        groupHeader.setCreDtTm(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        bkToCtmrStmt13.setGrpHdr(groupHeader);

        //:21: rel_ref_no
        Field21 field21 = mt940.getField21();
        if (field21 != null) {
            AccountStatement14 statement = factory.createAccountStatement14();
            statement.setAddtlStmtInf(field21.getValue());
            bkToCtmrStmt13.getStmt().add(statement);
        }

        // Create acctStmt14
        AccountStatement14 acctStmt14 = factory.createAccountStatement14();

        // :25: Account Number / IBAN -> Rpt.Acct.Id.Othr.Id
        Field25 field25 = mt940.getField25();
        if (field25 != null) {
            CashAccount43 account = factory.createCashAccount43();
            AccountIdentification4Choice accountId = factory.createAccountIdentification4Choice();

            if (isIban(field25.getValue())){
                accountId.setIBAN(field25.getValue());
            } else {
                GenericAccountIdentification1 genericAccount = factory.createGenericAccountIdentification1();
                genericAccount.setId(field25.getValue());
                accountId.setOthr(genericAccount);
            }
            account.setId(accountId);
            acctStmt14.setAcct(account);
        }

        // :28C: Statement Sequence Number -> Stmt.Id
        Field28C field28C = mt940.getField28C();
        if (field28C != null) {
            acctStmt14.setId(field28C.getValue());
        }

        //:60A:
        //this tagNot supported in prowide core older version (java 1.8)
        //Field60A field60A = mt940.getField60A();

        //:60M: ITBD Interim Booked Balance
        Field60M field60M = mt940.getField60M();
        if (field60M != null) {
            CashBalance8 openingBal = createInterimBookedBalance(factory, field60M);
            acctStmt14.getBal().add(openingBal);
        }

        // :60F: First Opening Balance -> Rpt.OpngBal
        Field60F field60F = mt940.getField60F();
        if (field60F != null) {
            CashBalance8 openingBal = createOpeningBalance(factory, field60F);
            acctStmt14.getBal().add(openingBal);
        }

        // :61: + :86: Statement Lines -> Rpt.Ntry (multiple)
        List<Field61> entries = mt940.getField61();
        List<Field86> infoFields = mt940.getField86();

        for (int i = 0; i < entries.size(); i++) {
            Field61 entryField = entries.get(i);
            ReportEntry15 entry = factory.createReportEntry15();

            // Amount and currency
            ActiveOrHistoricCurrencyAndAmount amt = new ActiveOrHistoricCurrencyAndAmount();
            amt.setValue(new BigDecimal(entryField.getAmount().replace(",", ".")));
            amt.setCcy(field60F != null ? field60F.getCurrency() : "OMR"); // Default to OMR
            entry.setAmt(amt);

            // Credit or Debit
            entry.setCdtDbtInd("C".equals(entryField.getDCMark()) ?
                    CreditDebitCode.CRDT : CreditDebitCode.DBIT);

            // Booking Date
            if (entryField.getValueDate() != null) {
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(entryField.getEntryDateAsCalendar().getTime());
                XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
                entry.setBookgDt(factory.createDateAndDateTime2Choice());
                entry.getBookgDt().setDt(xmlCal);
            }

            // :86: Info to Account Owner -> Ntry.AddtlNtryInf
            if (infoFields.size() > i && infoFields.get(i) != null) {
                entry.setAddtlNtryInf(infoFields.get(i).getValue());
            }

            acctStmt14.getNtry().add(entry);
        }

        // :62F: Closing Balance -> Rpt.ClsgBal
        Field62F field62F = mt940.getField62F();
        if (field62F != null) {
            CashBalance8 closingBal = createClosingBalance(factory, field62F);
            acctStmt14.getBal().add(closingBal);
        }

        // :64: Available Closing Balance -> Rpt.Avlbty
        Field64 field64 = mt940.getField64();
        if (field64 != null) {

                CashBalance8 closingBal = createClosingAvailableBalance(factory, field64);
                acctStmt14.getBal().add(closingBal);
        }

        //:65:	Opening Available Balance Next Statement
        List<Field65> field65List = mt940.getField65();
        if (field65List != null && !field65List.isEmpty()) {
            for (Field65 field65 : field65List) {
                CashBalance8 closingBal = createOpeningBalanceNxtDay(factory, field65);
                acctStmt14.getBal().add(closingBal);
            }
        }
        bkToCtmrStmt13.getStmt().add(acctStmt14);

        // Marshal to XML
        JAXBContext context = JAXBContext.newInstance(Document.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter sw = new StringWriter();
        marshaller.marshal(document, sw);
        return sw.toString();
    }


    public CashBalance8 createOpeningBalance(ObjectFactory factory, Field60F field) throws Exception {
        CashBalance8 balance = factory.createCashBalance8();

        ActiveOrHistoricCurrencyAndAmount amtAndDir = factory.createActiveOrHistoricCurrencyAndAmount();
        amtAndDir.setCcy(field.getCurrency());
        amtAndDir.setValue(new BigDecimal(field.getAmount().replace(",", ".")));
        balance.setAmt(amtAndDir);

        BalanceType13 type = factory.createBalanceType13();
        BalanceType10Choice balanceType10Choice = factory.createBalanceType10Choice();
        balanceType10Choice.setCd("OPBD");
        type.setCdOrPrtry(balanceType10Choice); // Opening Booked Balance
        balance.setTp(type);

        balance.setCdtDbtInd("C".equals(field.getComponent1()) ? CreditDebitCode.CRDT : CreditDebitCode.DBIT);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(field.getDateAsCalendar().getTime());
        DateAndDateTime2Choice dateAndDateTime2Choice = factory.createDateAndDateTime2Choice();
        dateAndDateTime2Choice.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
        balance.setDt(dateAndDateTime2Choice);

        return balance;
    }


    public CashBalance8 createOpeningBalanceNxtDay(ObjectFactory factory, Field65 field) throws Exception {
        CashBalance8 balance = factory.createCashBalance8();

        ActiveOrHistoricCurrencyAndAmount amtAndDir = factory.createActiveOrHistoricCurrencyAndAmount();
        amtAndDir.setCcy(field.getCurrency());
        amtAndDir.setValue(new BigDecimal(field.getAmount().replace(",", ".")));
        balance.setAmt(amtAndDir);

        BalanceType13 type = factory.createBalanceType13();
        BalanceType10Choice balanceType10Choice = factory.createBalanceType10Choice();
        balanceType10Choice.setCd("OPBD");
        type.setCdOrPrtry(balanceType10Choice); // Opening Booked Balance
        balance.setTp(type);

        balance.setCdtDbtInd("C".equals(field.getComponent1()) ? CreditDebitCode.CRDT : CreditDebitCode.DBIT);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(field.getDateAsCalendar().getTime());
        DateAndDateTime2Choice dateAndDateTime2Choice = factory.createDateAndDateTime2Choice();
        dateAndDateTime2Choice.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
        balance.setDt(dateAndDateTime2Choice);

        return balance;
    }


    public CashBalance8 createClosingBalance(ObjectFactory factory, Field62F field) throws Exception {
        CashBalance8 balance = factory.createCashBalance8();

        ActiveOrHistoricCurrencyAndAmount amtAndDir = factory.createActiveOrHistoricCurrencyAndAmount();
        amtAndDir.setCcy(field.getCurrency());
        amtAndDir.setValue(new BigDecimal(field.getAmount().replace(",", ".")));
        balance.setAmt(amtAndDir);

        BalanceType13 type = factory.createBalanceType13();
        BalanceType10Choice balanceType10Choice = factory.createBalanceType10Choice();
        balanceType10Choice.setCd("CLBD");
        type.setCdOrPrtry(balanceType10Choice); // Closing Booked Balance
        balance.setTp(type);

        balance.setCdtDbtInd("C".equals(field.getComponent1()) ? CreditDebitCode.CRDT : CreditDebitCode.DBIT);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(field.getDateAsCalendar().getTime());
        DateAndDateTime2Choice dateAndDateTime2Choice = factory.createDateAndDateTime2Choice();
        dateAndDateTime2Choice.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
        balance.setDt(dateAndDateTime2Choice);

        return balance;
    }


    public CashBalance8 createClosingAvailableBalance(ObjectFactory factory, Field64 field) throws Exception {
        CashBalance8 balance = factory.createCashBalance8();

        ActiveOrHistoricCurrencyAndAmount amtAndDir = factory.createActiveOrHistoricCurrencyAndAmount();
        amtAndDir.setCcy(field.getCurrency());
        amtAndDir.setValue(new BigDecimal(field.getAmount().replace(",", ".")));
        balance.setAmt(amtAndDir);

        BalanceType13 type = factory.createBalanceType13();
        BalanceType10Choice balanceType10Choice = factory.createBalanceType10Choice();
        balanceType10Choice.setCd("CLAV");
        type.setCdOrPrtry(balanceType10Choice); // Closing Booked Balance
        balance.setTp(type);

        balance.setCdtDbtInd("C".equals(field.getComponent1()) ? CreditDebitCode.CRDT : CreditDebitCode.DBIT);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(field.getDateAsCalendar().getTime());
        DateAndDateTime2Choice dateAndDateTime2Choice = factory.createDateAndDateTime2Choice();
        dateAndDateTime2Choice.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
        balance.setDt(dateAndDateTime2Choice);

        return balance;
    }


    public CashBalance8 createInterimBookedBalance(ObjectFactory factory, Field60M field) throws Exception {
        CashBalance8 balance = factory.createCashBalance8();

        ActiveOrHistoricCurrencyAndAmount amtAndDir = factory.createActiveOrHistoricCurrencyAndAmount();
        amtAndDir.setCcy(field.getCurrency());
        amtAndDir.setValue(new BigDecimal(field.getAmount().replace(",", ".")));
        balance.setAmt(amtAndDir);

        BalanceType13 type = factory.createBalanceType13();
        BalanceType10Choice balanceType10Choice = factory.createBalanceType10Choice();
        balanceType10Choice.setCd("ITBD");
        type.setCdOrPrtry(balanceType10Choice); // Closing Booked Balance
        balance.setTp(type);

        balance.setCdtDbtInd("C".equals(field.getComponent1()) ? CreditDebitCode.CRDT : CreditDebitCode.DBIT);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(field.getDateAsCalendar().getTime());
        DateAndDateTime2Choice dateAndDateTime2Choice = factory.createDateAndDateTime2Choice();
        dateAndDateTime2Choice.setDt(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
        balance.setDt(dateAndDateTime2Choice);

        return balance;
    }

    public boolean isIban(String accountId) {
        if (accountId == null || accountId.length() < 4) {
            return false;
        }
        // First two characters must be letters (A-Z)
        if (!Character.isLetter(accountId.charAt(0)) || !Character.isLetter(accountId.charAt(1))) {
            return false;
        }
        // Next two characters must be digits
        if (!Character.isDigit(accountId.charAt(2)) || !Character.isDigit(accountId.charAt(3))) {
            return false;
        }
        return true;
    }


}
