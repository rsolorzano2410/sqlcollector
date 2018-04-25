package sqlcollector.core.threads;

import sqlcollector.core.logs.L4j;
import sqlcollector.core.statistics.Capturer;
import sqlcollector.writer.influx.InfluxWriter;
import sqlcollector.xml.mapping.metrics.XmlDestDatabase;
import sqlcollector.xml.mapping.metrics.XmlMeasurement;
import sqlcollector.xml.mapping.metrics.XmlSourceDatabase;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MsmtThreadManager implements Runnable {

    private XmlSourceDatabase xmlSourceDatabase;
    private XmlDestDatabase xmlDestDatabase;
    private List<XmlMeasurement> lsXmlMeasurements;
	private long lIntervalMs;

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
        	L4j.getL4j().info("MsmtThreadManager. Init run.");
            long start_time=System.currentTimeMillis();
			try {
                L4j.getL4j().info("RSN. Calling this.launchThreads()");
                this.launchThreads(xmlSourceDatabase, xmlDestDatabase, lsXmlMeasurements);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            long elapsed = (System.currentTimeMillis() - start_time);
            try {
            	long lTimeToSleep = this.lIntervalMs - elapsed;
            	L4j.getL4j().info("MsmtThreadManager.run. elapsed: " + elapsed + ". sleeping for (ms): " + lTimeToSleep);
                Thread.sleep(lTimeToSleep);
            } catch (InterruptedException e) {
                start = false;
                e.printStackTrace();
            }
            L4j.getL4j().info("MsmtThreadManager. End run.");
        }
    }

    private void launchThreads(XmlSourceDatabase xmlSourceDatabase, XmlDestDatabase xmlDestDatabase, List<XmlMeasurement> lsXmlMeasurements) throws SQLException {
        L4j.getL4j().info("MsmtThreadManager. Init launchThreads()");
        String sSourceDatabaseId = xmlSourceDatabase.getId();
        long start_time=System.currentTimeMillis();
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
                long lTimeToWrite = xmlSourceDatabase.getIntervalSecs() * 1000;
                Capturer capturer = new Capturer(xmlSourceDatabase, xmlDestDatabase, xmlMeasurement);
                InfluxWriter influxWriter = new InfluxWriter(xmlDestDatabase, lTimeToWrite);
                Runnable worker = new MetricsCollector(sThreadId, capturer, influxWriter);
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
                long elapsed = (System.currentTimeMillis() - start_time);
                if(executor.isTerminated()){
                    waitToThreads = false;
                }
                if(elapsed == this.lIntervalMs && oneTime){
                    L4j.getL4j().critical("The time elapsed by thread is more than: " + this.lIntervalMs);
                    oneTime = false;
                }
            }
        }
        L4j.getL4j().info("MsmtThreadManager. End launchThreads()");
    }
    
}