package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * List of Measurements
 */
@XmlRootElement(name="Measurements")
public class XmlMeasurements {

    private List<XmlMeasurement> lsXmlMeasurements;

    public List<XmlMeasurement> getXmlMeasurements() {
        return lsXmlMeasurements;
    }

    @XmlElement(name="Measurement")
    public void setXmlMeasurements(List<XmlMeasurement> lsXmlMeasurements) {
        this.lsXmlMeasurements = lsXmlMeasurements;
    }
}
