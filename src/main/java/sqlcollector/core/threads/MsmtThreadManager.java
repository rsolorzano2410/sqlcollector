package sqlcollector.core.threads;

import sqlcollector.core.logs.L4j;
import sqlcollector.core.statistics.Capturer;
import sqlcollector.utils.Utils;
import sqlcollector.writer.influx.InfluxWriter;
import sqlcollector.xml.mapping.metrics.XmlDestDatabase;
import sqlcollector.xml.mapping.metrics.XmlMeasurement;
import sqlcollector.xml.mapping.metrics.XmlSourceDatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;

/*
 * This is the thread for each source database.
 * It launchs one thread for each measurement of this source database. 
 */
public class MsmtThreadManager implements Runnable {

    private XmlSourceDatabase xmlSourceDatabase;
    private XmlDestDatabase xmlDestDatabase;
    private List<XmlMeasurement> lsXmlMeasurements;
	private long lIntervalMs;
	private Connection connection;
	private InfluxDB influxDB;

    public MsmtThreadManager(XmlSourceDatabase xmlSourceDatabase, XmlDestDatabase xmlDestDatabase, List<XmlMeasurement> lsXmlMeasurements) throws SQLException {
    	L4j.getL4j().info("MsmtThreadManager. Init MsmtThreadManager.");
        this.xmlSourceDatabase = xmlSourceDatabase;
        this.xmlDestDatabase = xmlDestDatabase;
        this.lsXmlMeasurements = lsXmlMeasurements;
    	this.lIntervalMs = xmlSourceDatabase.getIntervalSecs() * 1000;
        L4j.getL4j().info("MsmtThreadManager. End MsmtThreadManager.");
    }

    public void run() {
        boolean start = true;
        while(start){
        	L4j.getL4j().info(this.xmlSourceDatabase.getId() + ". MsmtThreadManager. Init run.");
            long start_time=System.currentTimeMillis();
			try {
                L4j.getL4j().debug(this.xmlSourceDatabase.getId() + ". MsmtThreadManager. run. Calling this.launchThreads()");
                this.launchThreads(lsXmlMeasurements);
			} catch (SQLException e) {
				L4j.getL4j().error(this.xmlSourceDatabase.getId() + ". MsmtThreadManager. run. Error getting metrics: " + e.getMessage());
			} catch (InterruptedException e) {
				L4j.getL4j().error(this.xmlSourceDatabase.getId() + ". MsmtThreadManager. run. Interrupted while sleeping. Exception: " + e.getMessage());
			}
            long elapsed = (System.currentTimeMillis() - start_time);
            try {
            	long lTimeToSleep = this.lIntervalMs - elapsed;
            	L4j.getL4j().info(this.xmlSourceDatabase.getId() + ". MsmtThreadManager. run. elapsed: " + elapsed + ". sleeping for (ms): " + lTimeToSleep);
                Thread.sleep(lTimeToSleep);
            } catch (InterruptedException e) {
                start = false;
				L4j.getL4j().error(this.xmlSourceDatabase.getId() + ". MsmtThreadManager. run. Interrupted while sleeping. Exception: " + e.getMessage());
            }
            L4j.getL4j().info(this.xmlSourceDatabase.getId() + ". MsmtThreadManager. End run.");
        }
    }

    private void launchThreads(List<XmlMeasurement> lsXmlMeasurements) throws SQLException, InterruptedException {
        L4j.getL4j().info("MsmtThreadManager. Init launchThreads()");
		long lInitTime = System.currentTimeMillis();
		long lSpentTime = 0;
        getDBConnections();
		lSpentTime = System.currentTimeMillis() - lInitTime;
    	L4j.getL4j().info("getDBConnections. Spent time connecting to " + this.xmlSourceDatabase.getId() + ": " + lSpentTime);
    	long lTimeToRW = this.lIntervalMs - lSpentTime;
        String sSourceDatabaseId = xmlSourceDatabase.getId();
        if (lsXmlMeasurements != null) {
        	int iNumMeasurements = lsXmlMeasurements.size();
            ExecutorService executor = Executors.newFixedThreadPool(iNumMeasurements);
            L4j.getL4j().info("launchThreads. iNumMeasurements: " + iNumMeasurements);
            /*
             * For each source database get: 
             * - the destination database
             * - the list of measurements
             * - each measurement with the list of queries
             */
            for (XmlMeasurement xmlMeasurement: lsXmlMeasurements) {
                String sMeasurementId = xmlMeasurement.getId();
                String sThreadId = sSourceDatabaseId + "_" + sMeasurementId;
                Capturer capturer = new Capturer(sSourceDatabaseId, connection, xmlDestDatabase, xmlMeasurement);
                InfluxWriter influxWriter = new InfluxWriter(xmlDestDatabase, this.influxDB);
                Runnable worker = new MetricsCollector(sThreadId, capturer, influxWriter, lTimeToRW);
                executor.execute(worker);
            }
            executor.shutdown();
            try {
                executor.awaitTermination(50, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean waitToThreads = true;
            boolean oneTime = true;
            while (waitToThreads) {
                long elapsed = (System.currentTimeMillis() - lInitTime);
                if (executor.isTerminated()) {
                    waitToThreads = false;
                }
                if (elapsed == this.lIntervalMs && oneTime) {
                    L4j.getL4j().critical("The time elapsed by thread is more than: " + this.lIntervalMs);
                    oneTime = false;
                }
            }
        }
        L4j.getL4j().info("MsmtThreadManager. End launchThreads()");
    }
    
    private boolean getDBConnections() throws InterruptedException {
    	L4j.getL4j().info("Begin getDBConnections.");
        boolean bIsConnected = false;
		long lInitTime = System.currentTimeMillis();
		long lSpentTime = 0;
		bIsConnected = getConnection();
		lSpentTime = System.currentTimeMillis() - lInitTime;
    	L4j.getL4j().info("getDBConnections. Spent time connecting to " + this.xmlSourceDatabase.getId() + ": " + lSpentTime);
    	//Try connecting to InfluxDB
    	if (bIsConnected && lSpentTime < this.lIntervalMs) {
        	long lTimeToConnect = this.lIntervalMs - lSpentTime;
        	bIsConnected = getInfluxDB(lTimeToConnect);
    	} else {
    		bIsConnected = false;
    	}
		lSpentTime = System.currentTimeMillis() - lInitTime;
    	L4j.getL4j().info("End getDBConnections. Spent time: " + lSpentTime);
        return bIsConnected;
    }
    
    private boolean getConnection() throws InterruptedException {
    	L4j.getL4j().info("Begin getConnection.");
        boolean bIsConnected = false;
		long lInitTime = System.currentTimeMillis();
        long lReconnectTimeout = this.xmlSourceDatabase.getReconnectTimeoutSecs();
        long lReconnectTimeoutMs = lReconnectTimeout * 1000;
        while (!bIsConnected) {
        	try {
				this.connection = Utils.getOracleDBConnection(xmlSourceDatabase);
	            bIsConnected = Utils.isConnected(this.connection);
        	} catch (SQLException e) {
	            if(!bIsConnected) {
                	L4j.getL4j().warn("getConnection. Error connecting to DB: " + this.xmlSourceDatabase.getId()
                			+ ". Trying reconnection after " + lReconnectTimeout + " seconds.");
					Thread.sleep(lReconnectTimeoutMs);
	            }
        	}
        }
		long lSpentTime = System.currentTimeMillis() - lInitTime;
        L4j.getL4j().info("End getConnection. Time spent (ms):" + lSpentTime);
        return bIsConnected;
    }

    private boolean getInfluxDB(long lTimeToConnect) throws InterruptedException {
    	L4j.getL4j().debug("Begin getInfluxDB.");
        boolean bIsConnected = false;
		long lInitTime = System.currentTimeMillis();
		long lSpentTime = 0;
		
        long lReconnectTimeout = this.xmlDestDatabase.getReconnectTimeoutSecs();
        long lReconnectTimeoutMs = lReconnectTimeout * 1000;
        
    	String sDestDatabaseId = xmlDestDatabase.getId();
		L4j.getL4j().debug("DestDatabaseId: " + sDestDatabaseId);
        while (!bIsConnected) {
        	try {
        		this.influxDB = Utils.getInfluxDBConnection(xmlDestDatabase);
	            bIsConnected = Utils.isConnected(this.influxDB);
        	} catch (Exception e) {
				L4j.getL4j().warn("getInfluxDB. Exception: " + e.getMessage());
				lSpentTime = System.currentTimeMillis() - lInitTime;
	            if (!bIsConnected) {
		            if ((lSpentTime + lReconnectTimeoutMs) < lTimeToConnect) {
	                	L4j.getL4j().warn("getInfluxDB. Error pinging to host:port of DB: " + sDestDatabaseId
	                			+ ". Trying again after " + lReconnectTimeout + " seconds.");
						Thread.sleep(lReconnectTimeoutMs);
		            } else {
		            	L4j.getL4j().warn("getInfluxDB. The process spent " + lSpentTime + " ms trying connection to host:port of DB: " + sDestDatabaseId);
		            	break;
		            }
	            }
        	}
        }
    	lSpentTime = System.currentTimeMillis() - lInitTime;
    	L4j.getL4j().debug("End getInfluxDB. Time spent (ms): " + lSpentTime);
        return bIsConnected;
    }
    
}