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

public class Utils {

    public static Connection getOracleDBConnection(XmlSourceDatabase xmlSourceDatabase) throws SQLException {
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
                try {
                    Thread.sleep(lReconnectTimeoutSecs*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        L4j.getL4j().info("Connected to DB (Host: " + sHost + " Port: " + lPort + " DataBase: " + sDbName + ")");
        return oracleDB;
    }

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

}
