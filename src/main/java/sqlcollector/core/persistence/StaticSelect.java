package sqlcollector.core.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.Logger;

public class StaticSelect {

	private Logger logger;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private Boolean firstIteration;

    public StaticSelect(Logger logger, Connection connection, Boolean firstIteration) {
        this.logger = logger;
        this.connection = connection;
        this.firstIteration = firstIteration;
    }

    public ResultSet executeQuery(String select) {
        if(this.firstIteration) {
            logger.debug("Cache: " + Thread.currentThread().getName() + " QUERY: " + select);
        }
        logger.debug("executeQuery. select: " + select);
        ResultSet resultSet = null;
        try {
            long start_time=System.currentTimeMillis();
            this.preparedStatement = this.connection.prepareStatement(select);
            resultSet = this.preparedStatement.executeQuery();
            long serverIn = (System.currentTimeMillis() - start_time);
            logger.debug("StaticSelect.executeQuery. Worker " + Thread.currentThread().getName() + ". Spent time executing query (ms): " + serverIn);
        } catch (SQLException e) {
            logger.error("StaticSelect.executeQuery. Worker " + Thread.currentThread().getName() + ". Error executing query: " + select + ". Exception message: " + e.getMessage());
        }
        return resultSet;
    }

    public void close() {
        try {
        	if (this.preparedStatement != null && !this.preparedStatement.isClosed())
        		this.preparedStatement.close();
        } catch (SQLException e) {
        	logger.error("StaticSelect.close. Error closing preparedStatement: " + e.getMessage());
        }
    }
}
