package sqlcollector.core.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import sqlcollector.core.logs.L4j;

public class StaticSelect {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private Boolean firstIteration;

    public StaticSelect(Connection connection, Boolean firstIteration) {
        this.connection = connection;
        this.firstIteration = firstIteration;
    }

    public ResultSet executeQuery(String select) {
        if(this.firstIteration) {
            L4j.getL4j().debug("Cache: " + Thread.currentThread().getName() + " QUERY: " + select);
        }
        L4j.getL4j().debug("executeQuery. select: " + select);
        ResultSet resultSet = null;
        try {
            long start_time=System.currentTimeMillis();
            this.preparedStatement = this.connection.prepareStatement(select);
            resultSet = this.preparedStatement.executeQuery();
            long serverIn = (System.currentTimeMillis() - start_time);
            L4j.getL4j().debug("StaticSelect.executeQuery. Worker " + Thread.currentThread().getName() + ". Spent time executing query (ms): " + serverIn);
        } catch (SQLException e) {
            L4j.getL4j().error("StaticSelect.executeQuery. Worker " + Thread.currentThread().getName() + ". Error executing query: " + select + ". Exception message: " + e.getMessage());
        }
        return resultSet;
    }

    public void close() {
        try {
        	if (this.preparedStatement != null && !this.preparedStatement.isClosed())
        		this.preparedStatement.close();
        } catch (SQLException e) {
        	L4j.getL4j().error("StaticSelect.close. Error closing preparedStatement: " + e.getMessage());
        }
    }
}
