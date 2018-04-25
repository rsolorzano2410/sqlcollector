package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * List of IterateGroups
 */
@XmlRootElement(name="IterateGroups")
public class XmlIterateGroups {

    private List<XmlIterateGroup> lsXmlIterateGroups;

    public List<XmlIterateGroup> getXmlIterateGroups() {
        return lsXmlIterateGroups;
    }

    @XmlElement(name="IterateGroup")
    public void setXmlIterateGroups(List<XmlIterateGroup> lsXmlIterateGroups) {
        this.lsXmlIterateGroups = lsXmlIterateGroups;
    }
}
