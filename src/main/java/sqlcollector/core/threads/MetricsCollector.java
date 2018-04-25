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

public class MetricsCollector implements Runnable {

    private String sThreadId;
    private final Capturer capturer;
    private final InfluxWriter influxWriter;

    public MetricsCollector(String sThreadId, Capturer capturer, InfluxWriter influxWriter) {
        this.sThreadId = sThreadId;
        this.capturer = capturer;
        this.influxWriter = influxWriter;
    }

    public void run(){
    	L4j.getL4j().info("MetricsCollector. Init run. " + sThreadId);
        Thread.currentThread().setName(sThreadId);
        long start_time=System.currentTimeMillis();
        try {
			List<BatchPoints> lsBatchPoints = this.capturer.getBatchPointsList();
	        long lRetrieveTime = (System.currentTimeMillis() - start_time);
	    	L4j.getL4j().info("MetricsCollector. lRetrieveTime: " + lRetrieveTime);
	        this.influxWriter.writeToInflux(lsBatchPoints);
		} catch (SQLException e) {
			L4j.getL4j().error(sThreadId + ". MetricsCollector. Error getting metrics: " + e.getMessage());
		} catch (InterruptedException e) {
			L4j.getL4j().error(sThreadId + ". MetricsCollector. run. Interrupted while sleeping. Exception: " + e.getMessage());
		}
    	L4j.getL4j().info("MetricsCollector. End run. " + sThreadId);
    }
    
    /*
     * name Measmt = DBStats
     * fields retrieve_time, number_metrics
     * tags: DbId, 
     * extra_tags
     * inherit, heredar extra tags
     */
}