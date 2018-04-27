package sqlcollector.core.threads;

import sqlcollector.core.logs.L4j;
import sqlcollector.core.statistics.Capturer;
import sqlcollector.writer.influx.InfluxWriter;

import java.sql.SQLException;
import java.util.List;

import org.influxdb.dto.BatchPoints;

/*
 * This thread gets the metrics information from source database and saves it to destination database
 */

public class MetricsCollector extends Thread implements Runnable {

    private String sThreadId;
    private final Capturer capturer;
    private final InfluxWriter influxWriter;
    private long lTimeToRW;

    public MetricsCollector(String sThreadId, Capturer capturer, InfluxWriter influxWriter, long lTimeToRW) {
        L4j.getL4j().info("################################################################");
    	L4j.getL4j().info("# MetricsCollector. Creating thread for " + sThreadId);
        L4j.getL4j().info("################################################################");
        this.sThreadId = sThreadId;
        this.capturer = capturer;
        this.influxWriter = influxWriter;
        this.lTimeToRW = lTimeToRW;
    }

    public void run(){
    	L4j.getL4j().debug(sThreadId + ". MetricsCollector. Init run.");
        Thread.currentThread().setName(sThreadId);
		long lInitTime = System.currentTimeMillis();
        try {
			List<BatchPoints> lsBatchPoints = this.capturer.getBatchPointsList();
	        long lRetrieveTime = System.currentTimeMillis() - lInitTime;
	    	L4j.getL4j().info(sThreadId + ". MetricsCollector. lRetrieveTime: " + lRetrieveTime);
	    	//TODO: Send RetrieveTime to self metrics
	    	long lTimeToWrite = this.lTimeToRW - lRetrieveTime;
	    	if (lTimeToWrite > 0) {
		        this.influxWriter.writeToInflux(lsBatchPoints, lTimeToWrite);
		        long lWriteTime = System.currentTimeMillis() - lInitTime - lRetrieveTime;
		    	L4j.getL4j().info(sThreadId + ". MetricsCollector. lWriteTime: " + lWriteTime);
		    	//TODO: Send WriteTime to self metrics
	    	}
		} catch (SQLException e) {
			L4j.getL4j().error(sThreadId + ". MetricsCollector. Error getting metrics: " + e.getMessage());
		} catch (InterruptedException e) {
			L4j.getL4j().error(sThreadId + ". MetricsCollector. run. Interrupted while sleeping. Exception: " + e.getMessage());
		}
    	L4j.getL4j().debug(sThreadId + ". MetricsCollector. End run.");
    }
    
    /*
     * name Measmt = DBStats
     * fields retrieve_time, number_metrics
     * tags: DbId, 
     * extra_tags
     * inherit, heredar extra tags
     */
}