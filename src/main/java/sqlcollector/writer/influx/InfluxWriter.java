package sqlcollector.writer.influx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point.Builder;

import sqlcollector.core.logs.L4j;
import sqlcollector.utils.Utils;

public class InfluxWriter {

    private InfluxDB influxDB;
    private String sDestDatabaseName;
    private String sMeasurementId;
    private long lReconnectTimeoutMs;

    public InfluxWriter(InfluxDB influxDB, String sDestDatabaseName, String sMeasurementId, long lReconnectTimeoutMs) {
        this.influxDB = influxDB;
        this.sDestDatabaseName = sDestDatabaseName;
        this.sMeasurementId = sMeasurementId;
        this.lReconnectTimeoutMs = lReconnectTimeoutMs;
    }
    
    public String getDestDatabaseName() {
    	return this.sDestDatabaseName;
    }
    
    public void writeToInflux(List<BatchPoints> lsBatchPoints, long lTimeToWrite, String sDbIdTag) throws InterruptedException {
    	L4j.getL4j().debug("Begin writeToInflux.");
		long lInitTime = System.currentTimeMillis();
		long lSpentTime = 0;
		
    	try {
    		boolean bIsWritten = false;
    		while (!bIsWritten) {
    			try {
    		    	for (BatchPoints batchPoints: lsBatchPoints) {
        				influxDB.write(batchPoints);
    		    	}
    				lSpentTime = System.currentTimeMillis() - lInitTime;
    				L4j.getL4j().debug("writeToInflux. Time spent writting to influx (ms):" + lSpentTime);
    				if (lSpentTime > (lTimeToWrite / 2)) {
    					L4j.getL4j().warn("writeToInflux. Time spent writting to influx (ms):" + lSpentTime + ". Greater than half: " + lTimeToWrite);
    				}
    	    		
    				// Add write time to metrics
    	    		Map<String, String> tagsToAdd = new HashMap<String, String>();
    				if (sDbIdTag != null) {
        	    		tagsToAdd.put("DbId", sDbIdTag);
    				}
    				Map<String, Object> fieldsToAdd = new HashMap<String, Object>();
    				fieldsToAdd.put("WriteTimeMs", lSpentTime);
    				BatchPoints bpsWriteProcess = getBpsWriteProcess(tagsToAdd, fieldsToAdd);
    				influxDB.write(bpsWriteProcess);
    				
    				bIsWritten = true;
    			} catch (Exception e) {
    				L4j.getL4j().warn("writeToInflux. Exception. errormsg: " + e.getMessage());
    				lSpentTime = System.currentTimeMillis() - lInitTime;
    	            if (!bIsWritten) {
        	            if((lSpentTime + lReconnectTimeoutMs) < lTimeToWrite) {
                        	L4j.getL4j().warn("writeToInflux. Error writting to DB: " + sDestDatabaseName 
                        			+ ". Trying again after " + lReconnectTimeoutMs + " milliseconds.");
    						Thread.sleep(lReconnectTimeoutMs);
        	            } else {
    		            	L4j.getL4j().warn("writeToInflux. The process spent " + lSpentTime + " ms trying to write into DB: " + sDestDatabaseName);
    		            	break;
        	            }
    	            }
    			}
    		}
            lSpentTime = System.currentTimeMillis() - lInitTime;
            L4j.getL4j().debug("End writeToInflux. Time spent (ms):" + lSpentTime);
		} catch (Exception e) {
			L4j.getL4j().error("writeToInflux. Exception. errormsg: " + e.getMessage());
		}
    }

    private BatchPoints getBpsWriteProcess(Map<String, String> tagsToAdd, Map<String, Object> fieldsToAdd) {
		BatchPoints bpsWriteProcess = Utils.getEmptyBatchPoints(this.sDestDatabaseName);
		Builder bPoint = Utils.getInitialBPoint(this.sMeasurementId);
		bPoint = Utils.addTagsToBPoint(bPoint, tagsToAdd);
		bPoint = Utils.addFieldsToBPoint(bPoint, fieldsToAdd);
    	bpsWriteProcess = Utils.addBPointToBatchPoints(bpsWriteProcess, bPoint);
    	return bpsWriteProcess;
    }
}
