package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Xml Element used for an iterate group
 * Content:
 * id: attribute with the id of the iterate group, required
 * Query: xml element with the query to execute to iterate
 * Queries: xml element with the queries to execute for the iterate group
 */
@XmlRootElement(name="IterateGroup")
public class XmlIterateGroup {

	private String sId;
    private XmlQuery xmlQuery;
    private XmlQueries xmlQueries;

    public String getId() {
        return sId;
    }

    @XmlAttribute(name="id", required = true)
    public void setId(String sId) {
        this.sId = sId;
    }

    public XmlQuery getXmlQuery() {
        return xmlQuery;
    }

    @XmlElement(name="Query")
    public void setXmlQuery(XmlQuery xmlQuery) {
        this.xmlQuery = xmlQuery;
    }

    public XmlQueries getXmlQueries() {
        return xmlQueries;
    }

    @XmlElement(name="Queries")
    public void setXmlQueries(XmlQueries xmlQueries) {
        this.xmlQueries = xmlQueries;
    }

}
