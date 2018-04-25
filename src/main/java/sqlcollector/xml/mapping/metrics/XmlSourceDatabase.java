package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Values for Source Database
 */
@XmlRootElement(name="SourceDatabase")
public class XmlSourceDatabase {

	private String sId;
	private String sHost;
	private long lPort;
	private String sDbName;
	private String sUsername;
	private String sPassword;
	private boolean bSsl;
	private String sSslCertFilePath;
	private int iReconnectTimeoutSecs;
	private int iIntervalSecs;
	private String sDestDatabaseId;
	private XmlMeasurements xmlMeasurements;
	private String sLogFileName;
	private String sLogLevel;

    public String getId() {
        return sId;
    }

    @XmlAttribute(name="id", required = true)
    public void setId(String sId) {
        this.sId = sId;
    }

    public String getHost() {
        return sHost;
    }

    @XmlElement(name="Host")
    public void setHost(String sHost) {
        this.sHost = sHost;
    }

    public long getPort() {
        return lPort;
    }

    @XmlElement(name="Port")
    public void setPort(long lPort) {
        this.lPort = lPort;
    }

    public String getDbName() {
        return sDbName;
    }

    @XmlElement(name="DbName")
    public void setDbName(String sDbName) {
        this.sDbName = sDbName;
    }

    public String getUsername() {
        return sUsername;
    }

    @XmlElement(name="Username")
    public void setUsername(String sUsername) {
        this.sUsername = sUsername;
    }

    public String getPassword() {
        return sPassword;
    }

    @XmlElement(name="Password")
    public void setPassword(String sPassword) {
        this.sPassword = sPassword;
    }

    public boolean getSsl() {
        return bSsl;
    }

    @XmlElement(name="Ssl")
    public void setSsl(boolean bSsl) {
        this.bSsl = bSsl;
    }

    public String getSslCertFilePath() {
        return sSslCertFilePath;
    }

    @XmlElement(name="SslCertFilePath")
    public void setSslCertFilePath(String sSslCertFilePath) {
        this.sSslCertFilePath = sSslCertFilePath;
    }

    public int getReconnectTimeoutSecs() {
        return iReconnectTimeoutSecs;
    }

    @XmlElement(name="ReconnectTimeoutSecs")
    public void setReconnectTimeoutSecs(int iReconnectTimeoutSecs) {
        this.iReconnectTimeoutSecs = iReconnectTimeoutSecs;
    }

    public int getIntervalSecs() {
        return iIntervalSecs;
    }

    @XmlElement(name="IntervalSecs")
    public void setIntervalSecs(int iIntervalSecs) {
        this.iIntervalSecs = iIntervalSecs;
    }

    public String getDestDatabaseId() {
        return sDestDatabaseId;
    }

    @XmlElement(name="DestDatabaseId")
    public void setDestDatabaseId(String sDestDatabaseId) {
        this.sDestDatabaseId = sDestDatabaseId;
    }

    public XmlMeasurements getXmlMeasurements() {
        return xmlMeasurements;
    }

    @XmlElement(name="Measurements")
    public void setXmlMeasurements(XmlMeasurements xmlMeasurements) {
        this.xmlMeasurements = xmlMeasurements;
    }

    public String getLogFileName() {
        return sLogFileName;
    }

    @XmlElement(name="LogFileName")
    public void setLogFileName(String sLogFileName) {
        this.sLogFileName = sLogFileName;
    }

    public String getLogLevel() {
        return sLogLevel;
    }

    @XmlElement(name="LogLevel")
    public void setLogLevel(String sLogLevel) {
        this.sLogLevel = sLogLevel;
    }

}
