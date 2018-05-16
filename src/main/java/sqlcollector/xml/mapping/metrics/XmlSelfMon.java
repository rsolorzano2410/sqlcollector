package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Parameters for Self Monitoring
 */
@XmlRootElement(name="SelfMon")
public class XmlSelfMon {

    private boolean enabled;
    private int freq;
    private String sDestDatabaseId;
    private String sExtraTags;
	private String sLogFileName;
	private String sLogLevel;

    public boolean getEnabled() {
        return enabled;
    }

    @XmlElement(name="enabled")
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getFreq() {
        return freq;
    }

    @XmlElement(name="freq")
    public void setFreq(int freq) {
        this.freq = freq;
    }

    public String getDestDatabaseId() {
        return sDestDatabaseId;
    }

    @XmlElement(name="DestDatabaseId")
    public void setDestDatabaseId(String sDestDatabaseId) {
        this.sDestDatabaseId = sDestDatabaseId;
    }

    public String getExtraTags() {
        return sExtraTags;
    }

    @XmlElement(name="ExtraTags")
    public void setExtraTags(String sExtraTags) {
        this.sExtraTags = sExtraTags;
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
