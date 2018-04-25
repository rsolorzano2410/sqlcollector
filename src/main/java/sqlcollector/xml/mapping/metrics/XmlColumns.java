package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * List of Columns
 */
@XmlRootElement(name="Columns")
public class XmlColumns {

    private List<XmlColumn> lsXmlColumns;

    public List<XmlColumn> getXmlColumns() {
        return lsXmlColumns;
    }

    @XmlElement(name="Column")
    public void setXmlColumns(List<XmlColumn> lsXmlColumns) {
        this.lsXmlColumns = lsXmlColumns;
    }
}
