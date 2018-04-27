package sqlcollector.core.threads;

import sqlcollector.core.logs.L4j;
import sqlcollector.exception.SQLCollectorException;
import sqlcollector.xml.ReadConfXml;
import sqlcollector.xml.mapping.metrics.XmlDestDatabase;
import sqlcollector.xml.mapping.metrics.XmlMeasurement;
import sqlcollector.xml.mapping.metrics.XmlQuery;
import sqlcollector.xml.mapping.metrics.XmlSQLCollector;
import sqlcollector.xml.mapping.metrics.XmlSourceDatabase;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadManager implements Runnable {

    public ThreadManager() {
        L4j.getL4j().info("################################################################");
        L4j.getL4j().info("# ThreadManager. Creating thread to manage threads for source databases.");
        L4j.getL4j().info("################################################################");
    }
    
    public void run() {
        L4j.getL4j().debug("ThreadManager. Init run.");
		try {
			XmlSQLCollector xmlSQLCollector = ReadConfXml.getXmlSQLCollector();
			List<XmlSourceDatabase> lsXmlSourceDatabases = ReadConfXml.getSourceDatabases(xmlSQLCollector);
			List<XmlDestDatabase> lsXmlDestDatabases = ReadConfXml.getDestDatabases(xmlSQLCollector);
			List<XmlMeasurement> lsXmlMeasurements = ReadConfXml.getMeasurements(xmlSQLCollector);
			List<XmlQuery> lsXmlQueries = ReadConfXml.getQueries(xmlSQLCollector);
			try {
                L4j.getL4j().debug("Calling this.launchThreads()");
                this.launchThreads(lsXmlSourceDatabases, lsXmlDestDatabases, lsXmlMeasurements, lsXmlQueries);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SQLCollectorException e2) {
            L4j.getL4j().error("ThreadManager. run. Error reading configuration file. Exception: " + e2.getMessage());
		}
        L4j.getL4j().debug("ThreadManager. End run.");
    }

    private void launchThreads(List<XmlSourceDatabase> lsXmlSourceDatabases, List<XmlDestDatabase> lsXmlDestDatabases, List<XmlMeasurement> lsXmlMeasurementsDef, List<XmlQuery> lsXmlQueries) throws SQLException, InterruptedException {
        L4j.getL4j().debug("ThreadManager. Init launchThreads()");
        if (lsXmlSourceDatabases != null) {
        	int iNumSourceDatabases = lsXmlSourceDatabases.size();
            ExecutorService executor = Executors.newFixedThreadPool(iNumSourceDatabases);
            L4j.getL4j().info("################################################################");
            L4j.getL4j().info("# ThreadManager. Pool of " + iNumSourceDatabases + " threads created.");
            L4j.getL4j().info("################################################################");
            /*
             * For each source database get: 
             * - the destination database
             * - the list of measurements for this database
             * - each measurement including the list of queries for this measurement
             */
            for (XmlSourceDatabase xmlSourceDatabase: lsXmlSourceDatabases) {
            	XmlDestDatabase xmlDestDatabase = ReadConfXml.findXmlDestDatabase(xmlSourceDatabase, lsXmlDestDatabases);

            	List<XmlMeasurement> lsXmlMeasurements = ReadConfXml.findXmlMeasurements(xmlSourceDatabase, lsXmlMeasurementsDef, lsXmlQueries);
                L4j.getL4j().debug("lsXmlMeasurements.size(): " + lsXmlMeasurements.size());
                MsmtThreadManager worker = new MsmtThreadManager(xmlSourceDatabase, xmlDestDatabase, lsXmlMeasurements);
                executor.execute(worker);
            }
            executor.shutdown();
            try {
                executor.awaitTermination(50, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean waitToThreads = true;
            while (waitToThreads) {
                if(executor.isTerminated()){
                    waitToThreads = false;
                }
            }
        }
        L4j.getL4j().debug("ThreadManager. End launchThreads()");
    }
    
}