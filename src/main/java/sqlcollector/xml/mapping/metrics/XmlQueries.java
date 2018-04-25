package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * List of Queries
 */
@XmlRootElement(name="Queries")
public class XmlQueries {

    private List<XmlQuery> lsXmlQueries;

    public List<XmlQuery> getXmlQueries() {
        return lsXmlQueries;
    }

    @XmlElement(name="Query")
    public void setXmlQueries(List<XmlQuery> lsXmlQueries) {
        this.lsXmlQueries = lsXmlQueries;
    }
}
