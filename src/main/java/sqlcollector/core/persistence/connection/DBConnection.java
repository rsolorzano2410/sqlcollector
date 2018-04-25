package sqlcollector.core.persistence.connection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.Connection;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;

import okhttp3.OkHttpClient;
import sqlcollector.core.logs.L4j;

public class DBConnection {

    private static Connection connection;
	
    public static Connection getPgConnection(String sHost, long lPort, String sDbName, String sUsername, String sPassword) 
    		throws SQLException {
        String sJdbcDriver = "org.postgresql.Driver";
        String sConnectionString = "jdbc:postgresql://" + sHost + ":" + lPort + "/" + sDbName;
        connection = getJdbcConnection(sJdbcDriver, sConnectionString, sUsername, sPassword);
        return connection;
    }

    public static Connection getOracleConnection(String sHost, long lPort, String sDbName, String sUsername, String sPassword) 
    		throws SQLException {
        String sJdbcDriver = "oracle.jdbc.driver.OracleDriver";
        String sConnectionString = "jdbc:oracle:thin:@" + sHost + ":" + lPort + ":" + sDbName;
        connection = getJdbcConnection(sJdbcDriver, sConnectionString, sUsername, sPassword);
        return connection;
    }
    
    private static Connection getJdbcConnection(String sJdbcDriver, String sConnectionString, String sUsername, String sPassword) 
    		throws SQLException {
        if(connection == null || connection.isClosed()) {
            try {
                Class.forName(sJdbcDriver);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        	connection = DriverManager.getConnection(sConnectionString, sUsername, sPassword);
        }
        return connection;
    }

	public static InfluxDB getInfluxDBConnection(String sDbHost, Long lDbPort, String sDbDatabase, String sDbUser, String sDbPassword, 
			Boolean bSsl, String sSslCertFilePath) 
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException 
	{
        String sProtocol = "http";
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (bSsl.booleanValue()) {
        	sProtocol = "https";
        	clientBuilder = SslManager.getSSLClientBuilder(sSslCertFilePath);
        }
        String sConnectionString = sProtocol + "://" + sDbHost + ":" + lDbPort;
        L4j.getL4j().debug("getInfluxDB. Connecting to: " + sConnectionString);
    	InfluxDB influxDB = InfluxDBFactory.connect(sConnectionString, sDbUser, sDbPassword, clientBuilder);
    	return influxDB;
    }
    
	public static InfluxDB getInfluxDBConnection(String sDbHost, Long lDbPort, String sDbDatabase, String sDbUser, String sDbPassword, 
			Boolean bSsl, String sSslCertFilePath, long lReconnectTimeout, Long lTimeToConnect) 
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, InterruptedException 
	{
    	InfluxDB influxDB = getInfluxDBConnection(sDbHost, lDbPort, sDbDatabase, sDbUser, sDbPassword, bSsl, sSslCertFilePath);
		long lInitTime = System.currentTimeMillis();
		long lSpentTime = 0;
        long lReconnectTimeoutMs = lReconnectTimeout * 1000;
        boolean bIsConnected = false;
        while (!bIsConnected) {
        	try {
        		L4j.getL4j().debug("getInfluxDB. Calling ping");
		    	Pong pong = influxDB.ping();
		    	L4j.getL4j().debug("getInfluxDB. " + pong.toString());
            	bIsConnected = (pong != null && pong.isGood());
        	} catch (Exception e) {
        		L4j.getL4j().warn("getInfluxDB. Exception: " + e.getMessage());
				lSpentTime = System.currentTimeMillis() - lInitTime;
	            if (!bIsConnected) {
		            if ((lTimeToConnect == null) || (lSpentTime + lReconnectTimeoutMs) < lTimeToConnect.longValue()) {
		            	L4j.getL4j().warn("getInfluxDB. Error pinging to host:port:DB: " + sDbHost + ":" + lDbPort + ":" + sDbDatabase  
	                			+ ". Trying again after " + lReconnectTimeout + " seconds.");
						Thread.sleep(lReconnectTimeoutMs);
		            } else {
		            	L4j.getL4j().warn("getInfluxDB. The process spent " + lSpentTime + " ms trying connection to host:port:DB: " + sDbHost + ":" + lDbPort + ":" + sDbDatabase);
		            	break;
		            }
	            }
        	}
        }
    	return influxDB;
    }
    
}