package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Xml Element used for a measurement
 * Content:
 * id: attribute with the id of the measurement, required
 * Queries: xml element with the queries to execute for the measurement
 */
@XmlRootElement(name="Measurement")
public class XmlMeasurement {

	private String sId;
    private XmlQueries xmlQueries;

    public String getId() {
        return sId;
    }

    @XmlAttribute(name="id", required = true)
    public void setId(String sId) {
        this.sId = sId;
    }

    public XmlQueries getXmlQueries() {
        return xmlQueries;
    }

    @XmlElement(name="Queries")
    public void setXmlQueries(XmlQueries xmlQueries) {
        this.xmlQueries = xmlQueries;
    }

}
