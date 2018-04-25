package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Xml Element used for grouping a list of measurements
 * Content:
 * id: attribute with the id of the measurement group, required
 * Measurements: xml element with the list of measurements
 */
@XmlRootElement(name="MeasurementGroup")
public class XmlMeasurementGroup {

	private String sId;
    private XmlMeasurements xmlMeasurements;

    public String getId() {
        return sId;
    }

    @XmlAttribute(name="id", required = true)
    public void setId(String sId) {
        this.sId = sId;
    }

    public XmlMeasurements getXmlMeasurements() {
        return xmlMeasurements;
    }

    @XmlElement(name="Measurements")
    public void setXmlMeasurements(XmlMeasurements xmlMeasurements) {
        this.xmlMeasurements = xmlMeasurements;
    }

}
