package sqlcollector.xml;

import sqlcollector.core.logs.L4j;
import sqlcollector.exception.SQLCollectorException;
import sqlcollector.utils.constants.Constants;
import sqlcollector.xml.mapping.metrics.XmlDestDatabase;
import sqlcollector.xml.mapping.metrics.XmlDestDatabases;
import sqlcollector.xml.mapping.metrics.XmlIterateGroup;
import sqlcollector.xml.mapping.metrics.XmlIterateGroups;
import sqlcollector.xml.mapping.metrics.XmlLoggingConf;
import sqlcollector.xml.mapping.metrics.XmlMeasurement;
import sqlcollector.xml.mapping.metrics.XmlMeasurementGroup;
import sqlcollector.xml.mapping.metrics.XmlMeasurementGroups;
import sqlcollector.xml.mapping.metrics.XmlMeasurements;
import sqlcollector.xml.mapping.metrics.XmlQueries;
import sqlcollector.xml.mapping.metrics.XmlQuery;
import sqlcollector.xml.mapping.metrics.XmlSQLCollector;
import sqlcollector.xml.mapping.metrics.XmlSelfMon;
import sqlcollector.xml.mapping.metrics.XmlSourceDatabase;
import sqlcollector.xml.mapping.metrics.XmlSourceDatabases;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ReadConfXml {

    public ReadConfXml() { }

    public static List<XmlSourceDatabase> getSourceDatabases(XmlSQLCollector xmlSQLCollector) throws SQLCollectorException {
    	List<XmlSourceDatabase> lsXmlSourceDatabases = null;
    	if (xmlSQLCollector != null) {
        	XmlSourceDatabases xmlSourceDatabases = xmlSQLCollector.getXmlSourceDatabases();
        	if (xmlSourceDatabases != null) {
            	lsXmlSourceDatabases = xmlSourceDatabases.getXmlSourceDatabases();
        	}
    	}
    	return lsXmlSourceDatabases;
    }
    
    public static List<XmlDestDatabase> getDestDatabases(XmlSQLCollector xmlSQLCollector) throws SQLCollectorException {
    	List<XmlDestDatabase> lsXmlDestDatabases = null;
    	if (xmlSQLCollector != null) {
        	XmlDestDatabases xmlDestDatabases = xmlSQLCollector.getXmlDestDatabases();
        	if (xmlDestDatabases != null) {
            	lsXmlDestDatabases = xmlDestDatabases.getXmlDestDatabases();
        	}
    	}
    	return lsXmlDestDatabases;
    }
    
    public static List<XmlQuery> getQueries(XmlSQLCollector xmlSQLCollector) throws SQLCollectorException {
    	List<XmlQuery> lsXmlQueries = null;
    	if (xmlSQLCollector != null) {
        	XmlQueries xmlQueries = xmlSQLCollector.getXmlQueries();
        	if (xmlQueries != null) {
            	lsXmlQueries = xmlQueries.getXmlQueries();
        	}
    	}
    	return lsXmlQueries;
    }
    
    public static List<XmlMeasurement> getMeasurements(XmlSQLCollector xmlSQLCollector) throws SQLCollectorException {
    	List<XmlMeasurement> lsXmlMeasurements = null;
    	if (xmlSQLCollector != null) {
        	XmlMeasurements xmlMeasurements = xmlSQLCollector.getXmlMeasurements();
        	if (xmlMeasurements != null) {
            	lsXmlMeasurements = xmlMeasurements.getXmlMeasurements();
        	}
    	}
    	return lsXmlMeasurements;
    }
    
    public static List<XmlMeasurementGroup> getMeasurementGroups(XmlSQLCollector xmlSQLCollector) throws SQLCollectorException {
    	List<XmlMeasurementGroup> lsXmlMeasurementGroups = null;
    	if (xmlSQLCollector != null) {
        	XmlMeasurementGroups xmlMeasurementGroups = xmlSQLCollector.getXmlMeasurementGroups();
        	if (xmlMeasurementGroups != null) {
            	lsXmlMeasurementGroups = xmlMeasurementGroups.getXmlMeasurementGroups();
        	}
    	}
    	return lsXmlMeasurementGroups;
    }
    
    public static List<XmlIterateGroup> getIterateGroups(XmlSQLCollector xmlSQLCollector) throws SQLCollectorException {
    	List<XmlIterateGroup> lsXmlIterateGroups = null;
    	if (xmlSQLCollector != null) {
        	XmlIterateGroups xmlIterateGroups = xmlSQLCollector.getXmlIterateGroups();
        	if (xmlIterateGroups != null) {
            	lsXmlIterateGroups = xmlIterateGroups.getXmlIterateGroups();
        	}
    	}
    	return lsXmlIterateGroups;
    }
    
    public static XmlSelfMon getSelfMon(XmlSQLCollector xmlSQLCollector) throws SQLCollectorException {
    	XmlSelfMon xmlSelfMon = null;
    	if (xmlSQLCollector != null) {
    		xmlSelfMon = xmlSQLCollector.getXmlSelfMon();
    	}
    	return xmlSelfMon;
    }
    
    public static XmlLoggingConf getLoggingConf(XmlSQLCollector xmlSQLCollector) throws SQLCollectorException {
    	XmlLoggingConf xmlLoggingConf = null;
    	if (xmlSQLCollector != null) {
    		xmlLoggingConf = xmlSQLCollector.getXmlLoggingConf();
    	}
    	return xmlLoggingConf;
    }
    
    public static XmlQuery findXmlQueryById(String id, List<XmlQuery> lsXmlQueriesDef) {
    	XmlQuery xmlQueryFound = null;
        for(XmlQuery xmlQuery: lsXmlQueriesDef){
            if(id.equals(xmlQuery.getId())){
            	xmlQueryFound = xmlQuery;
            	break;
            }
        }
        return xmlQueryFound;
    }

    public static XmlDestDatabase findXmlDestDatabase(String sDestDatabaseId, List<XmlDestDatabase> lsXmlDestDatabases) {
    	XmlDestDatabase xmlDestDatabaseFound = null;
    	if (sDestDatabaseId != null) {
            xmlDestDatabaseFound = findXmlDestDatabaseById(sDestDatabaseId, lsXmlDestDatabases);
    	}
        return xmlDestDatabaseFound;
    }

    private static XmlDestDatabase findXmlDestDatabaseById(String sDestDatabaseId, List<XmlDestDatabase> lsXmlDestDatabases) {
    	XmlDestDatabase xmlDestDatabaseFound = null;
    	long lInitTime = System.currentTimeMillis();
        for(XmlDestDatabase xmlDestDatabase: lsXmlDestDatabases){
            if(sDestDatabaseId.equals(xmlDestDatabase.getId())){
            	xmlDestDatabaseFound = xmlDestDatabase;
            	break;
            }
        }
        long lSpentTime = System.currentTimeMillis() - lInitTime;
        L4j.getL4j().debug("findXmlDestDatabaseById. lSpentTime: " + lSpentTime);
        return xmlDestDatabaseFound;
    }

    public static List<XmlMeasurement> findXmlMeasurements(XmlSourceDatabase xmlSourceDatabase, List<XmlMeasurement> lsXmlMeasurementsDef, List<XmlQuery> lsXmlQueriesDef) {
    	List<XmlMeasurement> lsXmlMeasurements = xmlSourceDatabase.getXmlMeasurements().getXmlMeasurements();
    	return findXmlMeasurements(lsXmlMeasurements, lsXmlMeasurementsDef, lsXmlQueriesDef);
    }
    
    private static List<XmlMeasurement> findXmlMeasurements(List<XmlMeasurement> lsXmlMeasurements, List<XmlMeasurement> lsXmlMeasurementsDef, List<XmlQuery> lsXmlQueriesDef) {
    	List<XmlMeasurement> lsXmlMeasurementsRet = new LinkedList<XmlMeasurement>();
    	long lInitTime = System.currentTimeMillis();
        for (XmlMeasurement xmlMeasurement: lsXmlMeasurements) {
        	String sXmlMeasurementId = xmlMeasurement.getId();
        	XmlMeasurement xmlMeasurementRet = findXmlMeasurementById(sXmlMeasurementId, lsXmlMeasurementsDef, lsXmlQueriesDef);
        	lsXmlMeasurementsRet.add(xmlMeasurementRet);
        }
        long lSpentTime = System.currentTimeMillis() - lInitTime;
        L4j.getL4j().debug("findXmlMeasurements. lSpentTime: " + lSpentTime);
    	return lsXmlMeasurementsRet;
    }
    
    private static XmlMeasurement findXmlMeasurementById(String sXmlMeasurementId, List<XmlMeasurement> lsXmlMeasurementsDef, List<XmlQuery> lsXmlQueriesDef) {
    	XmlMeasurement xmlMeasurementRet = null;
        for(XmlMeasurement xmlMeasurement: lsXmlMeasurementsDef){
            if(sXmlMeasurementId.equals(xmlMeasurement.getId())){
            	L4j.getL4j().debug("findXmlMeasurements. found sXmlMeasurementId: " + sXmlMeasurementId);
            	xmlMeasurementRet = completeXmlQueries(xmlMeasurement, lsXmlQueriesDef);
            	break;
            }
        }
    	return xmlMeasurementRet;
    }
    
    /*
     * Complete XmlQueries.
     * XmlQueries inside XmlMeasurement only have id and Parameters, complete them with Statement and Columns
     */
    private static XmlMeasurement completeXmlQueries(XmlMeasurement xmlMeasurement, List<XmlQuery> lsXmlQueriesDef) {
    	XmlMeasurement xmlMeasurementRet = xmlMeasurement;
    	XmlQueries xmlQueries = xmlMeasurementRet.getXmlQueries();
    	List<XmlQuery> lsXmlQueries = xmlQueries.getXmlQueries();
    	List<XmlQuery> lsXmlQueriesCplt = completeXmlQueries(lsXmlQueries, lsXmlQueriesDef);
    	xmlQueries.setXmlQueries(lsXmlQueriesCplt);
    	xmlMeasurementRet.setXmlQueries(xmlQueries);
    	return xmlMeasurementRet;
    }
    
    private static List<XmlQuery> completeXmlQueries(List<XmlQuery> lsXmlQueries, List<XmlQuery> lsXmlQueriesDef) {
    	List<XmlQuery> lsXmlQueriesRet = new LinkedList<XmlQuery>();
    	for (XmlQuery xmlQuery: lsXmlQueries) {
    		String sXmlQueryId = xmlQuery.getId();
    		XmlQuery xmlQueryFound = findXmlQueryById(sXmlQueryId, lsXmlQueriesDef);
    		if (xmlQueryFound != null) {
    			xmlQuery.setXmlStatement(xmlQueryFound.getXmlStatement());
    			xmlQuery.setXmlColumns(xmlQueryFound.getXmlColumns());
    			L4j.getL4j().debug("completeXmlQueries. found sXmlQueryId: " + sXmlQueryId + ". statement: " + xmlQuery.getXmlStatement().getStatement());
    		}
    		lsXmlQueriesRet.add(xmlQuery);
    	}
    	return lsXmlQueriesRet;
    }
    
    public static XmlSQLCollector getXmlSQLCollector(String sConfigFilePath) throws SQLCollectorException {
        return unmarshal(new File(sConfigFilePath), XmlSQLCollector.class);
    }

    private static<E> E  unmarshal(File file, Class<E> type) throws SQLCollectorException {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        Object object;
        try {
            jaxbContext = JAXBContext.newInstance(type);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            object =  jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new SQLCollectorException("ReadConfXml Exception:", e);
        }
        if(object == null){
            throw new SQLCollectorException("ReadConfXml Exception: " + Constants.SQLCOLLECTOR_XML + " is null");
        } else {
            return type.cast(object);
        }
    }
}