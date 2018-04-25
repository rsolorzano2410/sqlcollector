package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * List of Parameters
 */
@XmlRootElement(name="Parameters")
public class XmlParameters {

    private List<XmlParameter> lsXmlParameters;

    public List<XmlParameter> getXmlParameters() {
        return lsXmlParameters;
    }

    @XmlElement(name="Parameter")
    public void setXmlParameters(List<XmlParameter> lsXmlParameters) {
        this.lsXmlParameters = lsXmlParameters;
    }
}
