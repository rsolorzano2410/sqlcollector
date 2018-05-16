package sqlcollector.core.threads;

import sqlcollector.core.statistics.Capturer;
import sqlcollector.utils.Utils;
import sqlcollector.writer.influx.InfluxWriter;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.influxdb.dto.BatchPoints;

/*
 * This thread gets the metrics information of one measurement from source database and saves it to destination database
 */

public class MetricsCollector extends Thread implements Runnable {

	private Logger logger;
    private String sThreadId;
    private final Capturer capturer;
    private final InfluxWriter influxWriter;
    private long lTimeToRW;

    /*
     * MetricsCollector. Constructor for metrics collector.
     * Parameters:
     * 	- String sThreadId: ID of the thread (sourcedatabaseid_msmtid)
     *  - Capturer capturer: the capturer to read data from source database
     *  - InfluxWriter influxWriter: the writer to write data to destination database
     *  - long lTimeToRW: maximum number of milliseconds to do the read and write operations,
     *  					(polling frequency - time spent getting connection to databases)
     */
    public MetricsCollector(Logger logger, String sThreadId, Capturer capturer, InfluxWriter influxWriter, long lTimeToRW) {
    	this.logger = logger;
        this.sThreadId = sThreadId;
        this.capturer = capturer;
        this.influxWriter = influxWriter;
        this.lTimeToRW = lTimeToRW;
        logger.info("################################################################");
    	logger.info("# MetricsCollector. Creating thread for " + sThreadId);
        logger.info("################################################################");
    }

    public void run(){
    	logger.debug(sThreadId + ". MetricsCollector. Init run.");
        Thread.currentThread().setName(sThreadId);
		long lInitTime = System.currentTimeMillis();
        try {
			List<BatchPoints> lsBatchPoints = this.capturer.getBatchPointsList();
	        long lReadTime = System.currentTimeMillis() - lInitTime;
	    	logger.info(sThreadId + ". MetricsCollector. lReadTime: " + lReadTime);
	    	if (lsBatchPoints != null && lsBatchPoints.size() > 0) {
	    		
	    		// Add read time and number of metrics to metrics
	    		Map<String, String> tagsToAdd = new HashMap<String, String>();
	    		tagsToAdd.put("DbId", this.capturer.getSourceDatabaseId());
	    		Map<String, Object> fieldsToAdd = new HashMap<String, Object>();
	    		fieldsToAdd.put("ReadTimeMs", lReadTime);
	    		fieldsToAdd.put("NumberMetrics", lsBatchPoints.size());
	    		BatchPoints bpsReadProcess = Utils.getBpsInfoProcess(this.influxWriter.getDestDatabaseName(), 
	    				this.capturer.getMeasurementId(), tagsToAdd, fieldsToAdd);
		    	lsBatchPoints.add(bpsReadProcess);
		    	
		    	long lTimeToWrite = this.lTimeToRW - lReadTime;
		    	if (lTimeToWrite > 0) {
			        this.influxWriter.writeToInflux(lsBatchPoints, lTimeToWrite, this.capturer.getSourceDatabaseId());
			        long lWriteTime = System.currentTimeMillis() - lInitTime - lReadTime;
			    	logger.info(sThreadId + ". MetricsCollector. lWriteTime: " + lWriteTime);
		    	}
	    	}
		} catch (SQLException e) {
			logger.error(sThreadId + ". MetricsCollector. Error getting metrics: " + e.getMessage());
		} catch (InterruptedException e) {
			logger.error(sThreadId + ". MetricsCollector. run. Interrupted while sleeping. Exception: " + e.getMessage());
		}
    	logger.debug(sThreadId + ". MetricsCollector. End run.");
    }
    
}