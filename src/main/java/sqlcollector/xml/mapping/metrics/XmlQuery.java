package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Xml Element used for a query
 * Content:
 * id: attribute with the id of the query, required
 * Statement: xml element with the statement to execute 
 * Columns: xml element with information of the statement columns
 * Parameters: xml element with information for the statement parameters
 * NOTE: Statement and Columns elements are used in query configuration, 
 * Parameters element is used when the query is included inside another element 
 */
@XmlRootElement(name="Query")
public class XmlQuery {

	private String sId;
    private XmlStatement xmlStatement;
    private XmlColumns xmlColumns;
    private XmlParameters xmlParameters;

    public String getId() {
        return sId;
    }

    @XmlAttribute(name="id", required = true)
    public void setId(String sId) {
        this.sId = sId;
    }

    public XmlStatement getXmlStatement() {
        return xmlStatement;
    }

    @XmlElement(name="Statement")
    public void setXmlStatement(XmlStatement xmlStatement) {
        this.xmlStatement = xmlStatement;
    }

    public XmlColumns getXmlColumns() {
        return xmlColumns;
    }

    @XmlElement(name="Columns")
    public void setXmlColumns(XmlColumns xmlColumns) {
        this.xmlColumns = xmlColumns;
    }

    public XmlParameters getXmlParameters() {
        return xmlParameters;
    }

    @XmlElement(name="Parameters")
    public void setXmlParameters(XmlParameters xmlParameters) {
        this.xmlParameters = xmlParameters;
    }

}
