package sqlcollector.utils;

import sqlcollector.core.logs.L4j;
import sqlcollector.core.persistence.connection.DBConnection;
import sqlcollector.xml.mapping.metrics.XmlDestDatabase;
import sqlcollector.xml.mapping.metrics.XmlSourceDatabase;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.SQLException;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Pong;

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
        while(!isConnectToDB) {
            oracleDB = DBConnection.getOracleConnection(sHost, lPort, sDbName, sUsername, sPassword);
            isConnectToDB = (oracleDB != null && !oracleDB.isClosed());
            if(!isConnectToDB) {
                Thread.sleep(lReconnectTimeoutSecs*1000);
            }
        }
        L4j.getL4j().info("Connected to DB (Host: " + sHost + " Port: " + lPort + " DataBase: " + sDbName + ")");
        return oracleDB;
    }

    /*
     * getInfluxDBConnection. Gets InfluxDB connection.
     */
	public static InfluxDB getInfluxDBConnection(XmlDestDatabase xmlDestDatabase) throws SQLException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, InterruptedException {
    	InfluxDB influxDB = null;
    	String sHost = xmlDestDatabase.getHost();
    	long lPort = xmlDestDatabase.getPort();
    	String sDbName = xmlDestDatabase.getDbName();
        String sUsername = xmlDestDatabase.getUsername();
        String sPassword = xmlDestDatabase.getPassword();
    	long lReconnectTimeoutSecs = xmlDestDatabase.getReconnectTimeoutSecs();
    	Long lTimeToConnect = null;
    	Boolean bSsl = xmlDestDatabase.getSsl();
    	String sSslCertFilePath = xmlDestDatabase.getSslCertFilePath();
    	
    	influxDB = DBConnection.getInfluxDBConnection(sHost, lPort, sDbName, sUsername, sPassword, bSsl, sSslCertFilePath, lReconnectTimeoutSecs, lTimeToConnect);
        L4j.getL4j().info("Connected to DB (Host: " + sHost + " Port: " + lPort + " DataBase: " + sDbName + ")");
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
	public static boolean isConnected(InfluxDB influxDB) throws SQLException {
		boolean bIsConnected = false;
    	L4j.getL4j().debug("getInfluxDB. Calling ping");
    	Pong pong = influxDB.ping();
    	L4j.getL4j().debug("getInfluxDB. " + pong.toString());
    	bIsConnected = (pong != null && pong.isGood());
		return bIsConnected;
	}

}
