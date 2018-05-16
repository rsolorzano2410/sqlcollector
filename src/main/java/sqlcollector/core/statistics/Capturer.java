package sqlcollector.core.statistics;

import sqlcollector.core.persistence.DynamicSelect;
import sqlcollector.utils.Utils;
import sqlcollector.xml.mapping.metrics.XmlColumn;
import sqlcollector.xml.mapping.metrics.XmlColumns;
import sqlcollector.xml.mapping.metrics.XmlMeasurement;
import sqlcollector.xml.mapping.metrics.XmlParameter;
import sqlcollector.xml.mapping.metrics.XmlParameters;
import sqlcollector.xml.mapping.metrics.XmlQueries;
import sqlcollector.xml.mapping.metrics.XmlQuery;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point.Builder;

/*
 * class for getting the metric info executing the queries
 */

public class Capturer {

	private Logger logger;
    private String sSourceDatabaseId;
    private String sXmlDestDatabaseName;
    private XmlMeasurement xmlMeasurement;
    private Connection connection;

    public Capturer(Logger logger, String sSourceDatabaseId, Connection connection, String sXmlDestDatabaseName, XmlMeasurement xmlMeasurement) {
        this.logger = logger;
        this.sSourceDatabaseId = sSourceDatabaseId;
        this.connection = connection;
        this.sXmlDestDatabaseName = sXmlDestDatabaseName;
        this.xmlMeasurement = xmlMeasurement;
    }
    
    public String getSourceDatabaseId() {
    	return this.sSourceDatabaseId;
    }
    
    public String getMeasurementId() {
    	return this.xmlMeasurement.getId();
    }
    
    private DynamicSelect getDynamicSelect(Boolean bFirstIteration, String sStatement, List<String> lsParameters,  List<String> lsParametersIN) {
        DynamicSelect dynamicSelect = new DynamicSelect(logger, connection, bFirstIteration);
		dynamicSelect.preparedStatement(sStatement, lsParameters, lsParametersIN);
    	return dynamicSelect;
    }
    
    private ResultSet getQueryResultSet(DynamicSelect dynamicSelect) {
    	ResultSet queryResultSet = null;
        queryResultSet = dynamicSelect.executeQuery();
    	return queryResultSet;
    }
    
    public List<BatchPoints> getBatchPointsList() throws SQLException {
    	List<BatchPoints> lsBatchPoints = new LinkedList<BatchPoints>();
    	List<String> lsParameters = new LinkedList<String>();
    	List<String> lsParametersIN = new LinkedList<String>();
        Boolean bFirstIteration = new Boolean(true);
		String sMsmtName = xmlMeasurement.getId();
		XmlQueries xmlQueries = xmlMeasurement.getXmlQueries();
		if (xmlQueries != null) {
			for (XmlQuery xmlQuery: xmlQueries.getXmlQueries()) {
				String sStatement = xmlQuery.getXmlStatement().getStatement();
				lsParameters.clear();
				lsParametersIN.clear();
				XmlParameters xmlParameters = xmlQuery.getXmlParameters();
				if (xmlParameters != null) {
					List<XmlParameter> lsXmlParameters = xmlParameters.getXmlParameters();
					for (XmlParameter xmlParameter: lsXmlParameters) {
						if (xmlParameter.getArgument() == null) {
							lsParameters.add(xmlParameter.getValue());
						} else if (xmlParameter.getArgument().equalsIgnoreCase("IN")) {
							lsParametersIN.add(xmlParameter.getValue());
						}
					}
				}
                DynamicSelect dynamicSelect = getDynamicSelect(bFirstIteration, sStatement, lsParameters, lsParametersIN);
                if (dynamicSelect != null) {
                    ResultSet queryResultSet = getQueryResultSet(dynamicSelect);
                    if (queryResultSet != null) {
                        BatchPoints batchPoints = getBatchPoints(queryResultSet, xmlQuery, sMsmtName);
                        lsBatchPoints.add(batchPoints);
                        queryResultSet.close();
                    }
                    dynamicSelect.close();
                }
			}
		}
    	return lsBatchPoints;
    }
    
    private BatchPoints getBatchPoints(ResultSet rsQuery, XmlQuery xmlQuery, String sMsmtName) throws SQLException {
        logger.debug("Capturer. Begin getBatchPoints.");
		long lInitTime = System.currentTimeMillis();
    	BatchPoints batchPoints = Utils.getEmptyBatchPoints(this.sXmlDestDatabaseName);
    	int iNumRegsQuery = 0;
    	int iNumBatchPoints = 0;
		while (rsQuery.next()) {
			iNumRegsQuery++;
			Builder bPoint = Utils.getInitialBPoint(sMsmtName);
			bPoint.tag("DbId", sSourceDatabaseId);
			bPoint.tag("QueryId", xmlQuery.getId());
			bPoint = Utils.addTagsToBPoint(bPoint, this.xmlMeasurement.getExtraTags());
			boolean bPointHasFields = false;
			XmlColumns xmlColumns = xmlQuery.getXmlColumns();
			if (xmlColumns != null) {
				List<XmlColumn> lsXmlColumns = xmlColumns.getXmlColumns();
				for (XmlColumn xmlColumn: lsXmlColumns) {
					String sFieldName = xmlColumn.getName();
					String sTagInflux = xmlColumn.getDestName();
					String sFieldInflux = xmlColumn.getDestName();
					String sTypeInflux = xmlColumn.getDataType();
					String sType = xmlColumn.getType();
					String sFieldValue = rsQuery.getString(sFieldName);
					if (sType.equalsIgnoreCase("tag")) {
						bPoint.tag(sTagInflux, sFieldValue);
					} else {
						if (sFieldValue != null) {
							if (sTypeInflux.equalsIgnoreCase("integer")) {
								bPoint.addField(sFieldInflux, rsQuery.getLong(sFieldName));
								bPointHasFields = true;
							} else if (sTypeInflux.equalsIgnoreCase("float")) {
								bPoint.addField(sFieldInflux, rsQuery.getDouble(sFieldName));
								bPointHasFields = true;
							} else if (sTypeInflux.equalsIgnoreCase("boolean")) {
								bPoint.addField(sFieldInflux, rsQuery.getBoolean(sFieldName));
								bPointHasFields = true;
							} else if (sTypeInflux.equalsIgnoreCase("string")) {
								bPoint.addField(sFieldInflux, sFieldValue);
								bPointHasFields = true;
							} else {
								//case "others"
								logger.warn("Capturer. getBatchPoints. Field " + sFieldInflux + " not added to: " + sMsmtName + ". " 
										+ sTypeInflux + " not supported in types_influx mapping parameters.");
							}
						}
					}
				}
			}
			if (bPointHasFields) {
				iNumBatchPoints++;
				batchPoints = Utils.addBPointToBatchPoints(batchPoints, bPoint);
			}
		}
		long lSpentTime = System.currentTimeMillis() - lInitTime;
		logger.debug("Capturer. End getBatchPoints. Measurement: " 
				+ sMsmtName + ". Query: " + xmlQuery.getId() + ". " 
				+ iNumRegsQuery + " records readed from Source DB. " 
				+ iNumBatchPoints + " batchPoints to be written into influx."
				+ " Time spent (ms):" + lSpentTime);
		logger.debug("Capturer. End getBatchPoints. " + iNumBatchPoints + " batchPoints to write: " + batchPoints.toString());
		if (iNumBatchPoints == 0) {
			logger.warn("Capturer. End getBatchPoints. NO batchPoints to write to: " + sMsmtName);
			batchPoints = null;
		}
    	return batchPoints;
    }
    
}
