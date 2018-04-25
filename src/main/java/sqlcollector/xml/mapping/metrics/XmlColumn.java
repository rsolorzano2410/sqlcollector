package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Columns of the query
 */
@XmlRootElement(name="Column")
public class XmlColumn {

    private String sName;
    private String sDestName;
    private String sType;
    private String sDataType;

    public String getName() {
        return sName;
    }

    @XmlElement(name="Name")
    public void setName(String sName) {
        this.sName = sName;
    }
    
    public String getDestName() {
        return sDestName;
    }

    @XmlElement(name="DestName")
    public void setDestName(String sDestName) {
        this.sDestName = sDestName;
    }
    
    public String getType() {
        return sType;
    }

    @XmlElement(name="Type")
    public void setType(String sType) {
        this.sType = sType;
    }

    public String getDataType() {
        return sDataType;
    }

    @XmlElement(name="DataType")
    public void setDataType(String sDataType) {
        this.sDataType = sDataType;
    }

}
