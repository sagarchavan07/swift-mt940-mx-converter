//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2025.04.26 at 10:59:03 AM IST 
//


package std.iso20022.xsd.camt_052_001_13;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OriginalBusinessQuery1 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OriginalBusinessQuery1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MsgId" type="{urn:iso:std:iso:20022:tech:xsd:camt.053.001.13}Max35Text"/>
 *         &lt;element name="MsgNmId" type="{urn:iso:std:iso:20022:tech:xsd:camt.053.001.13}Max35Text" minOccurs="0"/>
 *         &lt;element name="CreDtTm" type="{urn:iso:std:iso:20022:tech:xsd:camt.053.001.13}ISODateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OriginalBusinessQuery1", propOrder = {
    "msgId",
    "msgNmId",
    "creDtTm"
})
public class OriginalBusinessQuery1 {

    @XmlElement(name = "MsgId", required = true)
    protected String msgId;
    @XmlElement(name = "MsgNmId")
    protected String msgNmId;
    @XmlElement(name = "CreDtTm")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar creDtTm;

    /**
     * Gets the value of the msgId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMsgId() {
        return msgId;
    }

    /**
     * Sets the value of the msgId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMsgId(String value) {
        this.msgId = value;
    }

    /**
     * Gets the value of the msgNmId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMsgNmId() {
        return msgNmId;
    }

    /**
     * Sets the value of the msgNmId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMsgNmId(String value) {
        this.msgNmId = value;
    }

    /**
     * Gets the value of the creDtTm property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreDtTm() {
        return creDtTm;
    }

    /**
     * Sets the value of the creDtTm property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreDtTm(XMLGregorianCalendar value) {
        this.creDtTm = value;
    }

}
