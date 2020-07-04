package sqlcollector.core.threads;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point.Builder;

import sqlcollector.core.logs.L4j;
import sqlcollector.utils.Utils;
import sqlcollector.writer.influx.InfluxWriter;
import sqlcollector.xml.mapping.metrics.XmlDestDatabase;
import sqlcollector.xml.mapping.metrics.XmlLoggingConf;
import sqlcollector.xml.mapping.metrics.XmlSelfMon;

public class SelfMonThread implements Runnable {

	private Logger logger;
    private XmlSelfMon xmlSelfMon;
    private XmlDestDatabase xmlDestDatabase;

    public SelfMonThread(XmlLoggingConf xmlLoggingConf, XmlSelfMon xmlSelfMon, XmlDestDatabase xmlDestDatabase) {
    	this.logger = L4j.getLogger("SelfMon", xmlLoggingConf.getLogPath(), xmlSelfMon.getLogFileName(), 
				xmlSelfMon.getLogLevel(), xmlLoggingConf.getLogPattern(), xmlLoggingConf.getLogRollingPattern());
        logger.info("################################################################");
    	logger.info("# SelfMonThread. Creating thread for self monitoring.");
    	logger.info("# SelfMonThread. LogFileName: " + xmlSelfMon.getLogFileName() + ". LogLevel: " + xmlSelfMon.getLogLevel());
        logger.info("################################################################");
        this.xmlSelfMon = xmlSelfMon;
        this.xmlDestDatabase = xmlDestDatabase;
	}
	
    public void run() {
        long lFreqMs = this.xmlSelfMon.getFreq() * 1000;
        long lSleepTimeMs = lFreqMs;
        while (true) {
            logger.info("SelfMonThread. Init run.");
            long lInitTimeMs = System.currentTimeMillis();
            long lSpentTimeMs = 0;
            try {
	    		// Connect to InfluxDB with reconnections
            	InfluxDB influxDB = this.getInfluxDB();
            	if (influxDB != null) {
            		// Get self monitoring stats
            		String sMeasurementId = "SelfMonStats";
                	List<BatchPoints> lsBpsSelfMonStats = this.getSelfMonStats(sMeasurementId);
        	    	if (lsBpsSelfMonStats != null && lsBpsSelfMonStats.size() > 0) {
                        lSpentTimeMs = System.currentTimeMillis() - lInitTimeMs;

                        // Add read time to metrics
        	    		Map<String, String> tagsToAdd = new HashMap<String, String>();
        	    		Map<String, Object> fieldsToAdd = new HashMap<String, Object>();
                        long lReadTimeMs = lSpentTimeMs - lInitTimeMs;
        	    		fieldsToAdd.put("ReadTimeMs", lReadTimeMs);
        	    		BatchPoints bpsReadProcess = Utils.getBpsInfoProcess(this.xmlDestDatabase.getDbName(), 
        	    				sMeasurementId, tagsToAdd, fieldsToAdd);
        	    		lsBpsSelfMonStats.add(bpsReadProcess);
        		    	
        	    		long lTimeToWriteMs = lFreqMs - lSpentTimeMs;
        		    	if (lTimeToWriteMs > 0) {
        		    		// Write to InfluxDB with reconnections
        			        this.writeToInflux(influxDB, lsBpsSelfMonStats, sMeasurementId, lTimeToWriteMs);
        	                lSpentTimeMs = (System.currentTimeMillis() - lInitTimeMs);
        	                if (lSpentTimeMs < lFreqMs) {
        	                	lSleepTimeMs = lFreqMs - lSpentTimeMs;
        	                } else {
        	                    logger.warn("SelfMonThread.run. Spent time (ms): " + lSpentTimeMs + ". Greater than configured frequency (ms): " + lFreqMs);
        	                }
        	                logger.info("SelfMonThread.run. Spent time (ms): " + lSpentTimeMs + ". Sleeping for (ms): " + lSleepTimeMs);
        	                Thread.sleep(lSleepTimeMs);
                        } else {
                            logger.warn("SelfMonThread.run. Spent time (ms): " + lSpentTimeMs + ". Greater than configured frequency (ms): " + lFreqMs);
        		    	}
        	    	}
            	}
            } catch (InterruptedException e) {
				logger.error("SelfMonThread.run. Interrupted while sleeping. Exception: " + e.getMessage());
            }
            logger.info("SelfMonThread. End run.");
        }
    }
    
    private InfluxDB getInfluxDB() throws InterruptedException {
        logger.debug("SelfMonThread. Begin getInfluxDB.");
		long lInitTime = System.currentTimeMillis();
    	//Try connection to DestDB until it's done.
    	Long lTimeToConnect = null;
    	InfluxDB influxDB = Utils.getInfluxDBConnection(this.xmlDestDatabase, lTimeToConnect);
		long lSpentTime = System.currentTimeMillis() - lInitTime;
		logger.debug("SelfMonThread. End getInfluxDB. Time spent (ms):" + lSpentTime);
    	return influxDB;
    }
    
    private List<BatchPoints> getSelfMonStats(String sMeasurementId) {
        logger.debug("SelfMonThread. Begin getSelfMonStats.");
    	List<BatchPoints> lsBpsSelfMonStats = new LinkedList<BatchPoints>();
		long lInitTime = System.currentTimeMillis();
    	BatchPoints bpsSelfMonStats = Utils.getEmptyBatchPoints(this.xmlDestDatabase.getDbName());
		Builder bPoint = Utils.getInitialBPoint(sMeasurementId);
		addFieldsToBPoint(bPoint);
		bpsSelfMonStats = Utils.addBPointToBatchPoints(bpsSelfMonStats, bPoint);
    	lsBpsSelfMonStats.add(bpsSelfMonStats);
		long lSpentTime = System.currentTimeMillis() - lInitTime;
		logger.debug("SelfMonThread. End getSelfMonStats. Time spent (ms) getting self monitoring stats: " + lSpentTime);
    	return lsBpsSelfMonStats;
    }
    
    private void addFieldsToBPoint(Builder bPoint) {
    	logger.debug("SelfMonThread. Init addFieldsToBPoint.");
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        addFieldToBPoint(bPoint, "os.freememory", runtime.freeMemory());
        addFieldToBPoint(bPoint, "os.maxmemory", runtime.maxMemory());
        addFieldToBPoint(bPoint, "os.totalmemory", runtime.totalMemory());
        addFieldToBPoint(bPoint, "os.availableprocessors", runtime.availableProcessors());
        for (GarbageCollectorMXBean garbageCollectorMXBean: ManagementFactory.getGarbageCollectorMXBeans()){
            String name = garbageCollectorMXBean.getName().replace(" ", "_");
            addFieldToBPoint(bPoint, "jvm.gc." + name + ".gc_collection_count", garbageCollectorMXBean.getCollectionCount());
            addFieldToBPoint(bPoint, "jvm.gc." + name + ".gc_collection_time", garbageCollectorMXBean.getCollectionTime());
        }
        addFieldToBPoint(bPoint, "jvm.memory.heapUsage_init", memoryMXBean.getHeapMemoryUsage().getInit());
        addFieldToBPoint(bPoint, "jvm.memory.heapUsage_used", memoryMXBean.getHeapMemoryUsage().getUsed());
        addFieldToBPoint(bPoint, "jvm.memory.heapUsage_committed", memoryMXBean.getHeapMemoryUsage().getCommitted());
        addFieldToBPoint(bPoint, "jvm.memory.heapUsage_max", memoryMXBean.getHeapMemoryUsage().getMax());
        addFieldToBPoint(bPoint, "jvm.compiler.total_compilation_time", compilationMXBean.getTotalCompilationTime());
        addFieldToBPoint(bPoint, "jvm.classloading.class_count", classLoadingMXBean.getLoadedClassCount());
        for (MemoryPoolMXBean memoryPoolMXBean: ManagementFactory.getMemoryPoolMXBeans()){
            String name = memoryPoolMXBean.getName().replace(" ", "_");
            addFieldToBPoint(bPoint, "jvm.memoryPool." + name + ".usage_init", memoryPoolMXBean.getUsage().getInit());
            addFieldToBPoint(bPoint, "jvm.memoryPool." + name + ".usage_used", memoryPoolMXBean.getUsage().getUsed());
            addFieldToBPoint(bPoint, "jvm.memoryPool." + name + ".usage_committed", memoryPoolMXBean.getUsage().getCommitted());
            addFieldToBPoint(bPoint, "jvm.memoryPool." + name + ".usage_max", memoryPoolMXBean.getUsage().getMax());
        }
        addFieldToBPoint(bPoint, "jvm.threads.daemon_thread_count", threadMXBean.getDaemonThreadCount());
        addFieldToBPoint(bPoint, "jvm.threads.thread_count", threadMXBean.getThreadCount());
		// Add extra tags
        bPoint = Utils.addTagsToBPoint(bPoint, this.xmlSelfMon.getExtraTags());
    	logger.debug("SelfMonThread. End addFieldsToBPoint.");
    	return;
    }
	
    private void addFieldToBPoint(Builder bPoint, String sFieldName, long lFieldValue) {
    	logger.debug("SelfMonThread. addFieldToBPoint. Adding FieldName=FieldValue: " + sFieldName + "=" + lFieldValue);
    	bPoint.addField(sFieldName, lFieldValue);
    	return;
    }
	
    private void writeToInflux(InfluxDB influxDB, List<BatchPoints> lsBpsSelfMonStats, String sMeasurementId, long lTimeToWriteMs) throws InterruptedException {
        logger.debug("SelfMonThread. Begin writeToInflux.");
		long lInitTime = System.currentTimeMillis();
        InfluxWriter influxWriter = new InfluxWriter(influxDB, this.xmlDestDatabase.getDbName(), sMeasurementId, (this.xmlDestDatabase.getReconnectTimeoutSecs()*1000));
        influxWriter.writeToInflux(lsBpsSelfMonStats, lTimeToWriteMs, null);
		long lSpentTime = System.currentTimeMillis() - lInitTime;
		logger.debug("SelfMonThread. End getInfluxDB. Time spent (ms) writing self monitoring stats:" + lSpentTime);
    	return;
    }
    
}
