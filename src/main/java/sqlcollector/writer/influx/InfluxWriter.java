package sqlcollector.writer.influx;

import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;

import sqlcollector.core.logs.L4j;
import sqlcollector.xml.mapping.metrics.XmlDestDatabase;

public class InfluxWriter {

    private XmlDestDatabase xmlDestDatabase;
    private InfluxDB influxDB;

    public InfluxWriter(XmlDestDatabase xmlDestDatabase, InfluxDB influxDB) {
        this.xmlDestDatabase = xmlDestDatabase;
        this.influxDB = influxDB;
    }
    
    public void writeToInflux(List<BatchPoints> lsBatchPoints, long lTimeToWrite) throws InterruptedException {
    	L4j.getL4j().debug("Begin writeToInflux.");
		long lInitTime = System.currentTimeMillis();
		long lSpentTime = 0;
		
        long lReconnectTimeout = this.xmlDestDatabase.getReconnectTimeoutSecs();
        long lReconnectTimeoutMs = lReconnectTimeout * 1000;
        
    	String sDestDatabaseId = xmlDestDatabase.getId();
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
    				bIsWritten = true;
    			} catch (Exception e) {
    				L4j.getL4j().warn("writeToInflux. Exception. errormsg: " + e.getMessage());
    				lSpentTime = System.currentTimeMillis() - lInitTime;
    	            if (!bIsWritten) {
        	            if((lSpentTime + lReconnectTimeoutMs) < lTimeToWrite) {
                        	L4j.getL4j().warn("writeToInflux. Error writting to DB: " + sDestDatabaseId 
                        			+ ". Trying again after " + lReconnectTimeout + " seconds.");
    						Thread.sleep(lReconnectTimeoutMs);
        	            } else {
    		            	L4j.getL4j().warn("writeToInflux. The process spent " + lSpentTime + " ms trying to write into DB: " + sDestDatabaseId);
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

}
