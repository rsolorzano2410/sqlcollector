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
    private String prefix;

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

    public String getPrefix() {
        return prefix;
    }

    @XmlElement(name="prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
