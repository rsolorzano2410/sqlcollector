package sqlcollector.core.persistence;

import sqlcollector.core.logs.L4j;
import sqlcollector.utils.Utils;

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

    public void preparedStatement(String select, List<String> lsParameters, List<String> lsParametersIN) {
    	L4j.getL4j().debug("select: " + select + ". lsParameters.size(): " + lsParameters.size() + ". lsParametersIN.size(): " + lsParametersIN.size());
        StringBuilder selectIN = new StringBuilder();
        if(lsParametersIN != null) {
            for(int i = 0; i<lsParametersIN.size(); i++){
                selectIN.append("?,");
            }
            lsParameters.addAll(lsParametersIN);
            if (selectIN.length() > 0) {
            	select = select + " (" + selectIN.deleteCharAt(selectIN.length()-1) + ")";
            }
            if(this.fristIteration) {
                this.selecteInfo = select;
            }
        }
        try {
        	boolean bIsConnected = Utils.isConnected(this.connection);
        	if (bIsConnected) {
    			this.preparedStatement = this.connection.prepareStatement(select, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    	        int numberArgument = 1;
    	        for(String sParameter:lsParameters){
    	            if(this.fristIteration) {
    	                this.selecteInfo = this.selecteInfo.replaceFirst("\\?", sParameter);
    	            }
    	            if (lsParameters.size() > 0) {
    	            	L4j.getL4j().debug("select: " + select + ". lsParameters.size(): " + lsParameters.size() + ". numberArgument: " + numberArgument);
    	            	this.preparedStatement.setString(numberArgument++, sParameter);
    	            }
    	        }
    	        if(this.fristIteration) {
    	            L4j.getL4j().debug("Worker " + Thread.currentThread().getName() + " QUERY: " + this.selecteInfo);
    	        }

    	        this.isPrepared = true;
        	} else {
                L4j.getL4j().warn("DynamicSelect.preparedStatement. Worker " + Thread.currentThread().getName() + ". Connection is null or closed.");
        	}
		} catch (SQLException e) {
            L4j.getL4j().error("DynamicSelect.preparedStatement. Worker " + Thread.currentThread().getName() + ". Error preparing query. Exception message: " + e.getMessage());
		}
    }

    public ResultSet executeQuery() {
        ResultSet rs = null;
        if(isPrepared) {
            try {
                long start_time=System.currentTimeMillis();
            	if (this.preparedStatement != null && !this.preparedStatement.isClosed())
            		rs = this.preparedStatement.executeQuery();
                long serverIn = (System.currentTimeMillis() - start_time);
                L4j.getL4j().debug("DynamicSelect.executeQuery. Worker " + Thread.currentThread().getName() + ". Spent time executing query (ms): " + serverIn);
            } catch (SQLException e) {
                L4j.getL4j().error("DynamicSelect.executeQuery. Worker " + Thread.currentThread().getName() + ". Error executing query: " + this.selecteInfo + ". Exception message: " + e.getMessage());
            }
        }
        return rs;
    }

    public void close() {
        try {
        	if (this.preparedStatement != null && !this.preparedStatement.isClosed())
        		this.preparedStatement.close();
        } catch (SQLException e) {
        	L4j.getL4j().error("DynamicSelect.close. Error closing preparedStatement: " + e.getMessage());
        }
    }

}