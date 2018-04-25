package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class for configuration
 */
@XmlRootElement(name="SQLCollector")
public class XmlSQLCollector {

    private XmlSourceDatabases xmlSourceDatabases;
    private XmlDestDatabases xmlDestDatabases;
    private XmlQueries xmlQueries;
    private XmlMeasurements xmlMeasurements;
    private XmlMeasurementGroups xmlMeasurementGroups;
    private XmlIterateGroups xmlIterateGroups;
    private XmlSelfMon xmlSelfMon;

    public XmlSourceDatabases getXmlSourceDatabases() {
        return xmlSourceDatabases;
    }

    @XmlElement(name="SourceDatabases")
    public void setXmlSourceDatabases(XmlSourceDatabases xmlSourceDatabases) {
        this.xmlSourceDatabases = xmlSourceDatabases;
    }

    public XmlDestDatabases getXmlDestDatabases() {
        return xmlDestDatabases;
    }

    @XmlElement(name="DestDatabases")
    public void setXmlDestDatabases(XmlDestDatabases xmlDestDatabases) {
        this.xmlDestDatabases = xmlDestDatabases;
    }

    public XmlQueries getXmlQueries() {
        return xmlQueries;
    }

    @XmlElement(name="Queries")
    public void setXmlQueries(XmlQueries xmlQueries) {
        this.xmlQueries = xmlQueries;
    }

    public XmlMeasurements getXmlMeasurements() {
        return xmlMeasurements;
    }

    @XmlElement(name="Measurements")
    public void setXmlMeasurements(XmlMeasurements xmlMeasurements) {
        this.xmlMeasurements = xmlMeasurements;
    }

    public XmlMeasurementGroups getXmlMeasurementGroups() {
        return xmlMeasurementGroups;
    }

    @XmlElement(name="MeasurementGroups")
    public void setXmlMeasurementGroups(XmlMeasurementGroups xmlMeasurementGroups) {
        this.xmlMeasurementGroups = xmlMeasurementGroups;
    }

    public XmlIterateGroups getXmlIterateGroups() {
        return xmlIterateGroups;
    }

    @XmlElement(name="IterateGroups")
    public void setXmlIterateGroups(XmlIterateGroups xmlIterateGroups) {
        this.xmlIterateGroups = xmlIterateGroups;
    }

    public XmlSelfMon getXmlSelfMon() {
        return xmlSelfMon;
    }

    @XmlElement(name="SelfMon")
    public void setXmlSelfMon(XmlSelfMon xmlSelfMon) {
        this.xmlSelfMon = xmlSelfMon;
    }

}
