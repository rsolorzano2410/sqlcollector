package sqlcollector.core.threads;

import sqlcollector.exception.SQLCollectorException;
import sqlcollector.xml.ReadConfXml;
import sqlcollector.xml.mapping.metrics.XmlDestDatabase;
import sqlcollector.xml.mapping.metrics.XmlLoggingConf;
import sqlcollector.xml.mapping.metrics.XmlMeasurement;
import sqlcollector.xml.mapping.metrics.XmlQuery;
import sqlcollector.xml.mapping.metrics.XmlSQLCollector;
import sqlcollector.xml.mapping.metrics.XmlSelfMon;
import sqlcollector.xml.mapping.metrics.XmlSourceDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;

public class ThreadManager implements Runnable {
    
	private Logger logger;
	private XmlSQLCollector xmlSQLCollector;

    public ThreadManager(Logger logger, XmlSQLCollector xmlSQLCollector) {
    	this.logger = logger;
    	this.xmlSQLCollector = xmlSQLCollector;
        logger.info("################################################################");
        logger.info("# ThreadManager. Creating thread to manage threads for source databases.");
        logger.info("################################################################");
    }
    
    public void run() {
        logger.debug("ThreadManager. Init run.");
		try {
			XmlLoggingConf xmlLoggingConf = ReadConfXml.getLoggingConf(xmlSQLCollector);
			XmlSelfMon xmlSelfMon = ReadConfXml.getSelfMon(xmlSQLCollector);
			List<XmlSourceDatabase> lsXmlSourceDatabases = ReadConfXml.getSourceDatabases(xmlSQLCollector);
			List<XmlDestDatabase> lsXmlDestDatabases = ReadConfXml.getDestDatabases(xmlSQLCollector);
			List<XmlMeasurement> lsXmlMeasurements = ReadConfXml.getMeasurements(xmlSQLCollector);
			List<XmlQuery> lsXmlQueries = ReadConfXml.getQueries(xmlSQLCollector);
            logger.debug("ThreadManager. run. Calling this.launchThreads()");
            this.launchThreads(xmlLoggingConf, xmlSelfMon, lsXmlSourceDatabases, lsXmlDestDatabases, lsXmlMeasurements, lsXmlQueries);
		} catch (SQLCollectorException e) {
            logger.error("ThreadManager. run. Error reading configuration file. Exception: " + e.getMessage());
		}
        logger.debug("ThreadManager. End run.");
    }

    private void launchThreads(XmlLoggingConf xmlLoggingConf, XmlSelfMon xmlSelfMon, List<XmlSourceDatabase> lsXmlSourceDatabases, List<XmlDestDatabase> lsXmlDestDatabases, List<XmlMeasurement> lsXmlMeasurementsDef, List<XmlQuery> lsXmlQueries) {
        logger.debug("ThreadManager. Init launchThreads()");
        if (xmlSelfMon.getEnabled()) {
        	XmlDestDatabase xmlDestDatabase = ReadConfXml.findXmlDestDatabase(xmlSelfMon.getDestDatabaseId(), lsXmlDestDatabases);
        	SelfMonThread selfMonThread = new SelfMonThread(xmlLoggingConf, xmlSelfMon, xmlDestDatabase);
        	new Thread(selfMonThread, "SelfMonThread").start();
        }
        if (lsXmlSourceDatabases != null && lsXmlSourceDatabases.size() > 0) {
        	int iNumSourceDatabases = lsXmlSourceDatabases.size();
            ExecutorService executor = Executors.newFixedThreadPool(iNumSourceDatabases);
            logger.info("################################################################");
            logger.info("# ThreadManager. Pool of " + iNumSourceDatabases + " threads created.");
            logger.info("################################################################");
            /*
             * For each source database get: 
             * - the destination database
             * - the list of measurements for this database
             * - each measurement including the list of queries for this measurement
             */
            for (XmlSourceDatabase xmlSourceDatabase: lsXmlSourceDatabases) {
            	XmlDestDatabase xmlDestDatabase = ReadConfXml.findXmlDestDatabase(xmlSourceDatabase.getDestDatabaseId(), lsXmlDestDatabases);

            	List<XmlMeasurement> lsXmlMeasurements = ReadConfXml.findXmlMeasurements(xmlSourceDatabase, lsXmlMeasurementsDef, lsXmlQueries);
                logger.debug("ThreadManager.launchThreads. lsXmlMeasurements.size(): " + lsXmlMeasurements.size());
                MsmtThreadManager worker = new MsmtThreadManager(xmlLoggingConf, xmlSourceDatabase, xmlDestDatabase, lsXmlMeasurements);
                executor.execute(worker);
            }
            executor.shutdown();
            try {
                executor.awaitTermination(50, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                //logger.critical("ThreadManager. Error while awaiting termination of threads: " + e.getMessage());
                logger.fatal("ThreadManager. Error while awaiting termination of threads: " + e.getMessage());
            }
            boolean waitToThreads = true;
            while (waitToThreads) {
                if(executor.isTerminated()){
                    waitToThreads = false;
                }
            }
        }
        logger.debug("ThreadManager. End launchThreads()");
    }
    
}