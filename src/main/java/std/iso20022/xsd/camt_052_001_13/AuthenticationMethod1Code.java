//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2025.04.26 at 10:59:03 AM IST 
//


package std.iso20022.xsd.camt_052_001_13;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AuthenticationMethod1Code.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AuthenticationMethod1Code">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="UKNW"/>
 *     &lt;enumeration value="BYPS"/>
 *     &lt;enumeration value="NPIN"/>
 *     &lt;enumeration value="FPIN"/>
 *     &lt;enumeration value="CPSG"/>
 *     &lt;enumeration value="PPSG"/>
 *     &lt;enumeration value="MANU"/>
 *     &lt;enumeration value="MERC"/>
 *     &lt;enumeration value="SCRT"/>
 *     &lt;enumeration value="SNCT"/>
 *     &lt;enumeration value="SCNL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AuthenticationMethod1Code")
@XmlEnum
public enum AuthenticationMethod1Code {

    UKNW,
    BYPS,
    NPIN,
    FPIN,
    CPSG,
    PPSG,
    MANU,
    MERC,
    SCRT,
    SNCT,
    SCNL;

    public String value() {
        return name();
    }

    public static AuthenticationMethod1Code fromValue(String v) {
        return valueOf(v);
    }

}
