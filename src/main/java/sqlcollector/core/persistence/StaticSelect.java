package sqlcollector.core.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import sqlcollector.core.logs.L4j;

public class StaticSelect {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private Boolean fristIteration;

    public StaticSelect(Connection connection, Boolean fristIteration) {
        this.connection = connection;
        this.fristIteration = fristIteration;
    }

    public ResultSet executeQuery(String select) {
        if(this.fristIteration) {
            L4j.getL4j().debug("Cache: " + Thread.currentThread().getName() + " QUERY: " + select);
        }
        L4j.getL4j().debug("executeQuery. select: " + select);
        ResultSet resultSet = null;
        try {
            this.preparedStatement = this.connection.prepareStatement(select);
            resultSet = this.preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    public void close() {
        try {
            this.preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
