package sqlcollector.core.persistence;

import sqlcollector.core.logs.L4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DynamicSelect {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private boolean isPrepared;
    private Boolean fristIteration;
    private String selecteInfo;

    public DynamicSelect(Connection connection, Boolean fristIteration) {
        this.connection = connection;
        this.isPrepared = false;
        this.fristIteration = fristIteration;
    }

    public void preparedStatement(String select, List<String> arguments, List<String> parametersIN) {
        try {
        	L4j.getL4j().debug("RSN. select: " + select + ". arguments.size(): " + arguments.size() + ". parametersIN.size(): " + parametersIN.size());
            StringBuilder selectIN = new StringBuilder();
            if(parametersIN != null) {
                for(int i = 0; i<parametersIN.size(); i++){
                    selectIN.append("?,");
                }
                arguments.addAll(parametersIN);
                if (selectIN.length() > 0) {
                	select = select + " (" + selectIN.deleteCharAt(selectIN.length()-1) + ")";
                }
                if(this.fristIteration) {
                    this.selecteInfo = select;
                }
            }
        	L4j.getL4j().debug("RSN. after for. arguments.size(): " + arguments.size() + ". parametersIN.size(): " + parametersIN.size());
            this.preparedStatement = this.connection.prepareStatement(select, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int numberArgument = 1;
            //int numberArgument = 0;
            for(String argument:arguments){
                L4j.getL4j().debug("RSN. arguments.size(): " + arguments.size() + ". argument: " + argument);
                if(this.fristIteration) {
                    this.selecteInfo = this.selecteInfo.replaceFirst("\\?", argument);
                }
                //RSN 
                if (arguments.size() > 0) {
                	this.preparedStatement.setString(numberArgument++, argument);
                }
            }
            if(this.fristIteration) {
                L4j.getL4j().debug("Worker " + Thread.currentThread().getName() + " QUERY: " + this.selecteInfo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.isPrepared = true;
    }

    public ResultSet executeQuery() {
        ResultSet rs = null;
        if(isPrepared) {
            try {
                long start_time=System.currentTimeMillis();
                rs = this.preparedStatement.executeQuery();
                long serverIn = (System.currentTimeMillis() - start_time);
                L4j.getL4j().debug("Time executing query (ms): " + String.valueOf(serverIn));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return rs;
    }

    public void close() {
        try {
            this.preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}