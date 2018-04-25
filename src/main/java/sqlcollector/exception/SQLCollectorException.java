package sqlcollector.exception;

import sqlcollector.core.logs.L4j;

public class SQLCollectorException extends Exception {

    private static final long serialVersionUID = 1;

    public SQLCollectorException(String message, Throwable cause) {
        L4j.getL4j().error(message, cause);
    }

    public SQLCollectorException(String message) {
        L4j.getL4j().error(message);
    }

    public SQLCollectorException(Throwable cause) {
        L4j.getL4j().error("UNKNOWN ERROR", cause);
    }
}
