package sqlcollector.core;

import sqlcollector.core.logs.L4j;
import sqlcollector.core.threads.ThreadManager;
import sqlcollector.exception.SQLCollectorException;
import sqlcollector.utils.constants.Constants;
import sqlcollector.xml.ReadConfXml;
import sqlcollector.xml.mapping.metrics.XmlLoggingConf;
import sqlcollector.xml.mapping.metrics.XmlSQLCollector;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;

public class SQLCollector {
    public static void main(String[] args) {
        File f = new File(".");
        System.out.println(f.getAbsolutePath());

        // To avoid the error message:
        // "ERROR StatusLogger No log4j2 configuration file found. Using default configuration: logging only errors to the console."
        StatusLogger.getLogger().setLevel(Level.OFF);
        
		try {
			String sConfigFilePath = Constants.SQLCOLLECTOR_XML;
			if (System.getProperty("sqlcollector.configurationFile") != null) sConfigFilePath = System.getProperty("sqlcollector.configurationFile");
			System.out.println("SQLCollector. Configuration file: " + sConfigFilePath);
			XmlSQLCollector xmlSQLCollector = ReadConfXml.getXmlSQLCollector(sConfigFilePath);
			XmlLoggingConf xmlLoggingConf = ReadConfXml.getLoggingConf(xmlSQLCollector);
			Logger logger = L4j.getLogger("SQLCollector", xmlLoggingConf.getLogPath(), xmlLoggingConf.getLogFileName(), 
					xmlLoggingConf.getLogLevel(), xmlLoggingConf.getLogPattern(), xmlLoggingConf.getLogRollingPattern());

			logger.info("############################");
	    	logger.info("# Oracle metrics collector #");
	        logger.info("#       SQLCollector       #");
	        logger.info("#          v.0.5           #");
	        logger.info("#         01-06-18         #");
	        logger.info("############################");

	        ThreadManager threadManager = new ThreadManager(logger, xmlSQLCollector);
	        threadManager.run();
		} catch (SQLCollectorException e) {
	        System.err.println("SQLCollector. Error reading configuration file: " + e.getMessage());
		}
    }
}
