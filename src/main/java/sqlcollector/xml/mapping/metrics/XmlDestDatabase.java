package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Values for Destination Database
 */
@XmlRootElement(name="DestDatabase")
public class XmlDestDatabase {

	private String sId;
	private String sHost;
	private int iPort;
	private String sDbName;
	private String sUsername;
	private String sPassword;
	private boolean bSsl;
	private String sSslCertFilePath;
	private int iReconnectTimeoutSecs;

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

    public int getPort() {
        return iPort;
    }

    @XmlElement(name="Port")
    public void setPort(int iPort) {
        this.iPort = iPort;
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

}
