package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * List of SourceDatabases
 */
@XmlRootElement(name="SourceDatabases")
public class XmlSourceDatabases {

    private List<XmlSourceDatabase> lsXmlSourceDatabases;

    public List<XmlSourceDatabase> getXmlSourceDatabases() {
        return lsXmlSourceDatabases;
    }

    @XmlElement(name="SourceDatabase")
    public void setXmlSourceDatabases(List<XmlSourceDatabase> lsXmlSourceDatabases) {
        this.lsXmlSourceDatabases = lsXmlSourceDatabases;
    }
}
