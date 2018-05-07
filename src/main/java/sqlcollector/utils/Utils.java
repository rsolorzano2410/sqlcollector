package sqlcollector.utils;

import sqlcollector.core.logs.L4j;
import sqlcollector.core.persistence.connection.DBConnection;
import sqlcollector.xml.mapping.metrics.XmlDestDatabase;
import sqlcollector.xml.mapping.metrics.XmlSourceDatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Point.Builder;

public class Utils {

    /*
     * getOracleDBConnection. Gets Connection to Oracle DB with reconnect timeout specified into xmlSourceDatabase. Retries are done till connection is done.
     */
	public static Connection getOracleDBConnection(XmlSourceDatabase xmlSourceDatabase) throws SQLException, InterruptedException {
        Connection oracleDB = null;
        boolean isConnectToDB = false;
    	String sHost = xmlSourceDatabase.getHost();
    	long lPort = xmlSourceDatabase.getPort();
    	String sDbName = xmlSourceDatabase.getDbName();
        String sUsername = xmlSourceDatabase.getUsername();
        String sPassword = xmlSourceDatabase.getPassword();
    	long lReconnectTimeoutSecs = xmlSourceDatabase.getReconnectTimeoutSecs();
        L4j.getL4j().debug("Utils.getOracleDBConnection. xmlSourceDatabase.getId(): " + xmlSourceDatabase.getId() + ". isConnectToDB: " + isConnectToDB);
        while(!isConnectToDB) {
            oracleDB = DBConnection.getOracleConnection(sHost, lPort, sDbName, sUsername, sPassword);
            isConnectToDB = (oracleDB != null && !oracleDB.isClosed());
            L4j.getL4j().debug("Utils.getOracleDBConnection. xmlSourceDatabase.getId(): " + xmlSourceDatabase.getId() + ". isConnectToDB: " + isConnectToDB);
            if(!isConnectToDB) {
                Thread.sleep(lReconnectTimeoutSecs*1000);
            }
        }
        L4j.getL4j().info("Connected to DB (Host: " + sHost + " Port: " + lPort + " DataBase: " + sDbName + ")");
        return oracleDB;
    }

    /*
     * getInfluxDBConnection. Gets InfluxDB connection with reconnect timeout specified into xmlDestDatabase. 
     * If lTimeToConnect is null, retries are done till connection is done,
     * else retries are done during lTimeToConnect milliseconds.
     */
	public static InfluxDB getInfluxDBConnection(XmlDestDatabase xmlDestDatabase, Long lTimeToConnect) throws InterruptedException {
    	InfluxDB influxDB = null;
    	String sHost = xmlDestDatabase.getHost();
    	long lPort = xmlDestDatabase.getPort();
    	String sDbName = xmlDestDatabase.getDbName();
        String sUsername = xmlDestDatabase.getUsername();
        String sPassword = xmlDestDatabase.getPassword();
    	long lReconnectTimeoutSecs = xmlDestDatabase.getReconnectTimeoutSecs();
    	Boolean bSsl = xmlDestDatabase.getSsl();
    	String sSslCertFilePath = xmlDestDatabase.getSslCertFilePath();
    	
    	influxDB = DBConnection.getInfluxDBConnection(sHost, lPort, sDbName, sUsername, sPassword, bSsl, sSslCertFilePath, lReconnectTimeoutSecs, lTimeToConnect);
        return influxDB;
    }
	
	/*
	 * isConnected. Returns if the database is connected or not. 
	 */
	public static boolean isConnected(Connection connection) throws SQLException {
		boolean bIsConnected = false;
        bIsConnected = (connection != null && !connection.isClosed());
		return bIsConnected;
	}

	/*
	 * isConnected. Returns if the database is connected or not. 
	 */
	public static boolean isConnected(InfluxDB influxDB) {
		boolean bIsConnected = false;
    	L4j.getL4j().debug("getInfluxDB. Calling ping");
    	Pong pong = influxDB.ping();
    	L4j.getL4j().debug("getInfluxDB. " + pong.toString());
    	bIsConnected = (pong != null && pong.isGood());
		return bIsConnected;
	}

	/*
	 * getEmptyBatchPoints. Returns a BatchPoints object to be written to the destination InfluxDB. 
	 */
    public static BatchPoints getEmptyBatchPoints(String sDestDatabaseName) {
    	BatchPoints batchPoints = BatchPoints.database(sDestDatabaseName).build();
    	return batchPoints;
    }
    
	/*
	 * addBPointToBatchPoints. Adds a Builder Point as Point to BatchPoints. 
	 */
    public static BatchPoints addBPointToBatchPoints(BatchPoints batchPoints, Builder bPoint) {
		Point point = bPoint.build();
		batchPoints.point(point);
    	return batchPoints;
    }
    
	/*
	 * getInitialBPoint. Returns a Builder Point with current time in milliseconds for the measurement. 
	 */
    public static Builder getInitialBPoint(String sMeasurementId) {
		Builder bPoint = Point.measurement(sMeasurementId);
		bPoint.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		return bPoint;
    }
    
	/*
	 * addTagsToBPoint. Returns the Builder Point with the tags. 
	 * Parameters:
	 * 	- Builder bPoint: builder for the InfluxDB point
	 * 	- String sTags: tags in format tag1=value1,tag2=value2,..., tagN=valueN
	 */
    public static Builder addTagsToBPoint(Builder bPoint, String sTags) {
    	if (sTags != null) {
        	String[] asTags = sTags.split(",");
        	for (int i=0; i < asTags.length; i++) {
        		String[] asTagNameValue = asTags[i].split("=");
        		bPoint.tag(asTagNameValue[0], asTagNameValue[1]);
        	}
    	}
		return bPoint;
    }
    
	/*
	 * addTagsToBPoint. Returns the Builder Point with the tags. 
	 * Parameters:
	 * 	- Builder bPoint: builder for the InfluxDB point
	 * 	- Map<String, String> tagsToAdd: map with the tags to add
	 */
    public static Builder addTagsToBPoint(Builder bPoint, Map<String, String> tagsToAdd) {
		bPoint.tag(tagsToAdd);
		return bPoint;
    }
    
	/*
	 * addFieldsToBPoint. Returns the Builder Point with the fields. 
	 */
    public static Builder addFieldsToBPoint(Builder bPoint, Map<String, Object> fieldsToAdd) {
		bPoint.fields(fieldsToAdd);
		return bPoint;
    }
    
	/*
	 * getBpsInfoProcess. Returns BatchPoints with info about the process.
	 * Info must be passed with tags and fields. 
	 */
    public static BatchPoints getBpsInfoProcess(String sDestDatabaseName, String sMeasurementId, Map<String, String> tagsToAdd, Map<String, Object> fieldsToAdd) {
		BatchPoints bpsReadProcess = getEmptyBatchPoints(sDestDatabaseName);
		Builder bPoint = getInitialBPoint(sMeasurementId);
		bPoint = addTagsToBPoint(bPoint, tagsToAdd);
		bPoint = addFieldsToBPoint(bPoint, fieldsToAdd);
    	bpsReadProcess = addBPointToBatchPoints(bpsReadProcess, bPoint);
    	return bpsReadProcess;
    }
}
