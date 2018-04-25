package sqlcollector.core.logs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class L4j {

    private static final L4j instance;
    static Logger log;

    static {
        instance = new L4j();
    }

    private L4j(){
        log = LogManager.getLogger();
    }

    public static L4j getL4j() {
        return instance;
    }

    public void critical(String msg) {
        if (log.isFatalEnabled()) {
            log.fatal(msg);
        }
    }

    public void error(String msg) {
        if (log.isErrorEnabled()) {
            log.error(msg);
        }
    }

    public void error(String msg, Throwable t) {
        if (log.isErrorEnabled()) {
            log.error(msg, t);
        }
    }

    public void warn(String msg) {
        if (log.isWarnEnabled()) {
            log.warn((Object)msg);
        }
    }

    public void notice(String msg) {
        if (log.isInfoEnabled()) {
            log.info(msg);
        }
    }

    public void info(String msg) {
        if (log.isInfoEnabled()) {
            log.info(msg);
        }
    }

    public void debug(String msg) {
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }

    public void debug(String msg, Throwable t) {
        if (log.isDebugEnabled()) {
            log.debug(msg, t);
        }
    }
}
