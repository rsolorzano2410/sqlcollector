package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Xml element for Statement
 */
@XmlRootElement(name="Statement")
public class XmlStatement {

    private Boolean bCache;
    private Long lCacheTimeSecs;
    private String sStatement;

    public Boolean getCache() {
        return bCache;
    }

    @XmlAttribute(name="cache")
    public void setCache(Boolean bCache) {
        this.bCache = bCache;
    }

    public Long getCacheTimeSecs() {
        return lCacheTimeSecs;
    }

    @XmlAttribute(name="cacheTimeSecs")
    public void setCacheTimeSecs(Long lCacheTimeSecs) {
    	this.lCacheTimeSecs = lCacheTimeSecs;
    }

    public String getStatement() {
        return sStatement;
    }

    @XmlValue
    public void setStatement(String sStatement) {
        this.sStatement = sStatement;
    }
}
