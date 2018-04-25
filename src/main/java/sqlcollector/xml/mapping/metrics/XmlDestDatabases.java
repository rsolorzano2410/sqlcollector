package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * List of DestDatabases
 */
@XmlRootElement(name="DestDatabases")
public class XmlDestDatabases {

    private List<XmlDestDatabase> lsXmlDestDatabases;

    public List<XmlDestDatabase> getXmlDestDatabases() {
        return lsXmlDestDatabases;
    }

    @XmlElement(name="DestDatabase")
    public void setXmlDestDatabases(List<XmlDestDatabase> lsXmlDestDatabases) {
        this.lsXmlDestDatabases = lsXmlDestDatabases;
    }
}
