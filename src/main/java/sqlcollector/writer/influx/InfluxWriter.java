package sqlcollector.writer.influx;

import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;

import sqlcollector.core.logs.L4j;
import sqlcollector.utils.Utils;
import sqlcollector.xml.mapping.metrics.XmlDestDatabase;

public class InfluxWriter {

    private XmlDestDatabase xmlDestDatabase;
	private long lTimeToWrite;

    private L4j logger = L4j.getL4j();

    public InfluxWriter(XmlDestDatabase xmlDestDatabase, long lTimeToWrite) {
        this.xmlDestDatabase = xmlDestDatabase;
        this.lTimeToWrite = lTimeToWrite;
    }
    
    public void writeToInflux(List<BatchPoints> lsBatchPoints) throws InterruptedException {
    	for (BatchPoints batchPoints: lsBatchPoints) {
    		writeToInflux(batchPoints);
    	}
    }
    
    private void writeToInflux(BatchPoints batchPoints) throws InterruptedException {
    	logger.debug(". Begin writeToInflux.");
		long lInitTime = System.currentTimeMillis();
		long lSpentTime = 0;
		
        long lReconnectTimeout = this.xmlDestDatabase.getReconnectTimeoutSecs();
        long lReconnectTimeoutMs = lReconnectTimeout * 1000;
        
        InfluxDB influxDB = null;
    	String sDestDatabaseId = xmlDestDatabase.getId();
		L4j.getL4j().debug("DestDatabaseId: " + sDestDatabaseId);
    	try {
			influxDB = Utils.getInfluxDBConnection(xmlDestDatabase);
        	lSpentTime = System.currentTimeMillis() - lInitTime;
            logger.info(". writeToInflux. Connected to DB: " + sDestDatabaseId + ". Time spent (ms):" + lSpentTime);
    		boolean bIsWritten = false;
    		while (!bIsWritten) {
    			try {
    				influxDB.write(batchPoints);
    				lSpentTime = System.currentTimeMillis() - lInitTime;
    				logger.info(". writeToInflux. Time spent writting to influx (ms):" + lSpentTime);
    				if (lSpentTime > (lTimeToWrite / 2)) {
    					logger.warn(". writeToInflux. Time spent writting to influx (ms):" + lSpentTime + ". Greater than half: " + lTimeToWrite);
    				}
    				bIsWritten = true;
    			} catch (Exception e) {
    				logger.warn(". writeToInflux. Exception. errormsg: " + e.getMessage());
    				lSpentTime = System.currentTimeMillis() - lInitTime;
    	            if (!bIsWritten) {
        	            if((lSpentTime + lReconnectTimeoutMs) < lTimeToWrite) {
                        	logger.warn(". writeToInflux. Error writting to DB: " + sDestDatabaseId 
                        			+ ". Trying again after " + lReconnectTimeout + " seconds.");
    						Thread.sleep(lReconnectTimeoutMs);
        	            } else {
    		            	logger.warn(". writeToInflux. The process spent " + lSpentTime + " ms trying to write into DB: " + sDestDatabaseId);
    		            	break;
        	            }
    	            }
    			}
    		}
            lSpentTime = System.currentTimeMillis() - lInitTime;
            logger.debug(". End writeToInflux. Time spent (ms):" + lSpentTime);
		} catch (Exception e) {
			logger.error(". writeToInflux. Exception. errormsg: " + e.getMessage());
		}
    }

}
