package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Parameters for queries.
 * In the same order as the query conditions.
 * Attributes:
 *   - type: column or literal - default literal
 *           if type is literal or does not exist, the value of the parameter is passed literally for the query
 *           if type is column, the value of the parameter must contains the name of a column of the statement element
 *                        then, the value from this column is passed in for the query
 *   - argument: IN or null - default null
 *           if argument is IN, the value of the parameter is used for the WHERE ... IN condition of the query
 *           for this version, the query only can contain one WHERE ... IN condition and it must be at the end of the query
 * 
 */
@XmlRootElement(name="Parameter")
public class XmlParameter {

    private String sValue;
    private String sType;
    private String sArgument;

    public String getValue() {
        return sValue;
    }

    @XmlValue
    public void setValue(String sValue) {
        this.sValue = sValue;
    }

    public String getType() {
        return sType;
    }

    @XmlAttribute(name = "type")
    public void setType(String sType) {
        this.sType = sType;
    }

    public String getArgument() {
        return sArgument;
    }

    @XmlAttribute(name = "argument")
    public void setArgument(String sArgument) {
        this.sArgument = sArgument;
    }
}
