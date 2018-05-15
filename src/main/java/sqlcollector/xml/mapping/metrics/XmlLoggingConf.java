package sqlcollector.xml.mapping.metrics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Parameters for logging
 * 
	LogPath: string with the path for logs
	LogFileName: string with the name of the generic log file
	LogLevel: string with the log level for the log file. valid values: trace,debug,info,warn,error,fatal
	LogPattern: string with the pattern for logs
	LogRollingPattern: string with the rolling pattern for logs.
		The logs to be rolled will be saved joining: LogPath+LogFileName+LogRollingPattern+".log.gz"
 * 
 * 
 */
@XmlRootElement(name="LoggingConf")
public class XmlLoggingConf {

	private String sLogPath;
	private String sLogFileName;
	private String sLogLevel;
	private String sLogPattern;
	private String sLogRollingPattern;

    public String getLogPath() {
        return sLogPath;
    }

    @XmlElement(name="LogPath")
    public void setLogPath(String sLogPath) {
        this.sLogPath = sLogPath;
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

    public String getLogPattern() {
        return sLogPattern;
    }

    @XmlElement(name="LogPattern")
    public void setLogPattern(String sLogPattern) {
        this.sLogPattern = sLogPattern;
    }

    public String getLogRollingPattern() {
        return sLogRollingPattern;
    }

    @XmlElement(name="LogRollingPattern")
    public void setLogRollingPattern(String sLogRollingPattern) {
        this.sLogRollingPattern = sLogRollingPattern;
    }

}
