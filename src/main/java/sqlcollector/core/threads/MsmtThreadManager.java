package sqlcollector.core.threads;

import sqlcollector.core.logs.L4j;
import sqlcollector.core.statistics.Capturer;
import sqlcollector.utils.Utils;
import sqlcollector.writer.influx.InfluxWriter;
import sqlcollector.xml.mapping.metrics.XmlDestDatabase;
import sqlcollector.xml.mapping.metrics.XmlLoggingConf;
import sqlcollector.xml.mapping.metrics.XmlMeasurement;
import sqlcollector.xml.mapping.metrics.XmlSourceDatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;

/*
 * This is the thread for each source database.
 * It launchs one thread for each measurement of this source database. 
 */
public class MsmtThreadManager extends Thread implements Runnable {

	private Logger logger;
    private XmlSourceDatabase xmlSourceDatabase;
    private XmlDestDatabase xmlDestDatabase;
    private List<XmlMeasurement> lsXmlMeasurements;
	private long lIntervalMs;
	private Connection connection;
	private InfluxDB influxDB;

    public MsmtThreadManager(XmlLoggingConf xmlLoggingConf, XmlSourceDatabase xmlSourceDatabase, XmlDestDatabase xmlDestDatabase, List<XmlMeasurement> lsXmlMeasurements) {
    	this.logger = L4j.getLogger(xmlSourceDatabase.getId(), xmlLoggingConf.getLogPath(), xmlSourceDatabase.getLogFileName(), 
    			xmlSourceDatabase.getLogLevel(), xmlLoggingConf.getLogPattern(), xmlLoggingConf.getLogRollingPattern());
        logger.info("################################################################");
    	logger.info("# MsmtThreadManager. Creating thread for " + xmlSourceDatabase.getId());
    	logger.info("# MsmtThreadManager. LogFileName: " + xmlSourceDatabase.getLogFileName() + ". LogLevel: " + xmlSourceDatabase.getLogLevel());
        logger.info("################################################################");
        this.xmlSourceDatabase = xmlSourceDatabase;
        this.xmlDestDatabase = xmlDestDatabase;
        this.lsXmlMeasurements = lsXmlMeasurements;
    	this.lIntervalMs = xmlSourceDatabase.getIntervalSecs() * 1000;
        logger.debug("MsmtThreadManager. End MsmtThreadManager.");
    }

    public void run() {
        boolean start = true;
        while(start){
        	logger.debug(xmlSourceDatabase.getId() + ". MsmtThreadManager. Init run.");
            long start_time=System.currentTimeMillis();
			try {
                logger.debug(xmlSourceDatabase.getId() + ". MsmtThreadManager. run. Calling this.launchThreads()");
                this.launchThreads(lsXmlMeasurements);
			} catch (SQLException e) {
				logger.error(xmlSourceDatabase.getId() + ". MsmtThreadManager. run. Error getting metrics: " + e.getMessage());
			} catch (InterruptedException e) {
				logger.error(xmlSourceDatabase.getId() + ". MsmtThreadManager. run. Interrupted while sleeping. Exception: " + e.getMessage());
			}
            long elapsed = (System.currentTimeMillis() - start_time);
            try {
            	long lTimeToSleep = this.lIntervalMs - elapsed;
                if (lTimeToSleep <= 0) {
                	lTimeToSleep = this.lIntervalMs;
                }
            	logger.info(xmlSourceDatabase.getId() + ". MsmtThreadManager. run. elapsed: " + elapsed + ". sleeping for (ms): " + lTimeToSleep);
            	Thread.sleep(lTimeToSleep);
            } catch (InterruptedException e) {
                start = false;
				logger.error(xmlSourceDatabase.getId() + ". MsmtThreadManager. run. Interrupted while sleeping. Exception: " + e.getMessage());
            }
            logger.debug(xmlSourceDatabase.getId() + ". MsmtThreadManager. End run.");
        }
    }

    private void launchThreads(List<XmlMeasurement> lsXmlMeasurements) throws SQLException, InterruptedException {
        logger.debug("MsmtThreadManager. Init launchThreads()");
		long lInitTime = System.currentTimeMillis();
		long lSpentTime = 0;
		boolean bIsConnected = getDBConnections();
		lSpentTime = System.currentTimeMillis() - lInitTime;
    	long lTimeToRW = this.lIntervalMs - lSpentTime;
		if (bIsConnected && lTimeToRW > 0 && lsXmlMeasurements != null) {
        	int iNumMeasurements = lsXmlMeasurements.size();
            ExecutorService executor = Executors.newFixedThreadPool(iNumMeasurements);
            logger.info("################################################################");
            logger.info("# MsmtThreadManager. Pool of " + iNumMeasurements + " threads created for " + xmlSourceDatabase.getId());
            logger.info("################################################################");
            /*
             * For each source database get: 
             * - the destination database
             * - the list of measurements
             * - each measurement with the list of queries
             */
            for (XmlMeasurement xmlMeasurement: lsXmlMeasurements) {
                String sMeasurementId = xmlMeasurement.getId();
                String sThreadId = xmlSourceDatabase.getId() + "_" + sMeasurementId;
                Capturer capturer = new Capturer(logger, xmlSourceDatabase.getId(), connection, xmlDestDatabase.getDbName(), xmlMeasurement);
                InfluxWriter influxWriter = new InfluxWriter(this.influxDB, this.xmlDestDatabase.getDbName(), sMeasurementId, (this.xmlDestDatabase.getReconnectTimeoutSecs()*1000));
                MetricsCollector worker = new MetricsCollector(logger, sThreadId, capturer, influxWriter, lTimeToRW);
                executor.execute(worker);
            }
            executor.shutdown();
            try {
                executor.awaitTermination(50, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                //logger.critical(xmlSourceDatabase.getId() + ". Error while awaiting termination of threads: " + e.getMessage());
                logger.fatal(xmlSourceDatabase.getId() + ". Error while awaiting termination of threads: " + e.getMessage());
            }
            boolean waitToThreads = true;
            boolean oneTime = true;
            while (waitToThreads) {
                long elapsed = (System.currentTimeMillis() - lInitTime);
                if(executor.isTerminated()){
                    waitToThreads = false;
                }
                if(elapsed == this.lIntervalMs && oneTime){
                    //logger.critical(xmlSourceDatabase.getId() + ". The time elapsed by thread is more than: " + this.lIntervalMs);
                    logger.fatal(xmlSourceDatabase.getId() + ". The time elapsed by thread is more than: " + this.lIntervalMs);
                    oneTime = false;
                }
            }
		}
        logger.debug("MsmtThreadManager. End launchThreads()");
    }
    
    private boolean getDBConnections() throws InterruptedException {
    	logger.debug("Begin getDBConnections.");
        boolean bIsConnected = false;
		long lInitTime = System.currentTimeMillis();
		long lSpentTime = 0;
		bIsConnected = getConnection();
		lSpentTime = System.currentTimeMillis() - lInitTime;
    	//Try connecting to InfluxDB
    	if (bIsConnected && lSpentTime < this.lIntervalMs) {
        	long lTimeToConnect = this.lIntervalMs - lSpentTime;
        	bIsConnected = getInfluxDB(lTimeToConnect);
    	} else {
    		bIsConnected = false;
    	}
		lSpentTime = System.currentTimeMillis() - lInitTime;
    	logger.info("End getDBConnections. Spent time: " + lSpentTime);
        return bIsConnected;
    }
    
    private boolean getConnection() throws InterruptedException {
    	logger.debug("Begin getConnection.");
        boolean bIsConnected = false;
		long lInitTime = System.currentTimeMillis();
        long lReconnectTimeout = this.xmlSourceDatabase.getReconnectTimeoutSecs();
        long lReconnectTimeoutMs = lReconnectTimeout * 1000;
        while (!bIsConnected) {
        	try {
				this.connection = Utils.getOracleDBConnection(xmlSourceDatabase);
	            bIsConnected = Utils.isConnected(this.connection);
        	} catch (SQLException e) {
            	logger.warn("MsmtThreadManager. getConnection. SQLException: " + e.getMessage()
    				+ ". bIsConnected: " + bIsConnected);
	            if(!bIsConnected) {
                	logger.warn("MsmtThreadManager. getConnection. Error connecting to DB: " + this.xmlSourceDatabase.getId()
                			+ ". Trying reconnection after " + lReconnectTimeout + " seconds.");
					Thread.sleep(lReconnectTimeoutMs);
	            }
        	}
        }
		long lSpentTime = System.currentTimeMillis() - lInitTime;
    	logger.info("End getConnection. Spent time connecting to " + xmlSourceDatabase.getId() + ": " + lSpentTime);
        return bIsConnected;
    }

    private boolean getInfluxDB(Long lTimeToConnect) throws InterruptedException {
    	logger.debug("Begin getInfluxDB. lTimeToConnect: " + lTimeToConnect);
        boolean bIsConnected = false;
		long lInitTime = System.currentTimeMillis();
		long lSpentTime = 0;
		
		this.influxDB = Utils.getInfluxDBConnection(xmlDestDatabase, lTimeToConnect);
		if (this.influxDB != null) {
			bIsConnected = Utils.isConnected(this.influxDB);
		}

    	lSpentTime = System.currentTimeMillis() - lInitTime;
    	logger.info("End getInfluxDB. Connected=" + bIsConnected + ". Spent time connecting to " + xmlDestDatabase.getId() + ": " + lSpentTime);
        return bIsConnected;
    }
    
}