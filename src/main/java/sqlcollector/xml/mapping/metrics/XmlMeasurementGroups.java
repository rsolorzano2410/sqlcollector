package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * List of MeasurementGroups
 */
@XmlRootElement(name="MeasurementGroups")
public class XmlMeasurementGroups {

    private List<XmlMeasurementGroup> lsXmlMeasurementGroups;

    public List<XmlMeasurementGroup> getXmlMeasurementGroups() {
        return lsXmlMeasurementGroups;
    }

    @XmlElement(name="MeasurementGroup")
    public void setXmlMeasurementGroups(List<XmlMeasurementGroup> lsXmlMeasurementGroups) {
        this.lsXmlMeasurementGroups = lsXmlMeasurementGroups;
    }
}
