package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Parameters for queries
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
