package sqlcollector.core.statistics;

import sqlcollector.core.logs.L4j;
import sqlcollector.core.persistence.DynamicSelect;
import sqlcollector.utils.Utils;
import sqlcollector.xml.mapping.metrics.XmlColumn;
import sqlcollector.xml.mapping.metrics.XmlColumns;
import sqlcollector.xml.mapping.metrics.XmlDestDatabase;
import sqlcollector.xml.mapping.metrics.XmlMeasurement;
import sqlcollector.xml.mapping.metrics.XmlQueries;
import sqlcollector.xml.mapping.metrics.XmlQuery;
import sqlcollector.xml.mapping.metrics.XmlSourceDatabase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

/*
 * class for getting the metric info executing the queries
 */

public class Capturer {

    private XmlSourceDatabase xmlSourceDatabase;
    private XmlDestDatabase xmlDestDatabase;
    private XmlMeasurement xmlMeasurement;

    private L4j logger = L4j.getL4j();

    public Capturer(XmlSourceDatabase xmlSourceDatabase, XmlDestDatabase xmlDestDatabase, XmlMeasurement xmlMeasurement) {
        this.xmlSourceDatabase = xmlSourceDatabase;
        this.xmlDestDatabase = xmlDestDatabase;
        this.xmlMeasurement = xmlMeasurement;
    }
    
    private DynamicSelect getDynamicSelect(Connection connection, Boolean bFirstIteration, String sStatement, List<String> arguments,  List<String> argumentsIN) {
        DynamicSelect dynamicSelect = new DynamicSelect(connection, bFirstIteration);
        dynamicSelect.preparedStatement(sStatement, arguments, argumentsIN);
    	return dynamicSelect;
    }
    
    private ResultSet getQueryResultSet(DynamicSelect dynamicSelect) {
    	ResultSet queryResultSet = null;
        queryResultSet = dynamicSelect.executeQuery();
    	return queryResultSet;
    }
    
    public List<BatchPoints> getBatchPointsList() throws SQLException {
    	List<BatchPoints> lsBatchPoints = new LinkedList<BatchPoints>();
    	//lsArguments and lsArgumentsIN empty lists for this version of code
    	List<String> lsArguments = new LinkedList<String>();
    	List<String> lsArgumentsIN = new LinkedList<String>();
        Boolean bFirstIteration = new Boolean(true);
        Connection conn = Utils.getOracleDBConnection(xmlSourceDatabase);
		String sMsmtName = xmlMeasurement.getId();
		XmlQueries xmlQueries = xmlMeasurement.getXmlQueries();
		if (xmlQueries != null) {
			for (XmlQuery xmlQuery: xmlQueries.getXmlQueries()) {
				String sStatement = xmlQuery.getXmlStatement().getStatement();
                DynamicSelect dynamicSelect = getDynamicSelect(conn, bFirstIteration, sStatement, lsArguments, lsArgumentsIN);
                ResultSet queryResultSet = getQueryResultSet(dynamicSelect);
                BatchPoints batchPoints = getBatchPoints(queryResultSet, sMsmtName, xmlQuery);
                lsBatchPoints.add(batchPoints);
                queryResultSet.close();
                dynamicSelect.close();
			}
		}
    	return lsBatchPoints;
    }
    
    private BatchPoints getBatchPoints(ResultSet rsQuery, String sMsmtName, XmlQuery xmlQuery) throws SQLException {
        logger.debug(". Begin getBatchPoints.");
		long lInitTime = System.currentTimeMillis();
    	BatchPoints batchPoints = BatchPoints.database(this.xmlDestDatabase.getDbName()).build();
    	int iNumRegsQuery = 0;
    	int iNumBatchPoints = 0;
		while (rsQuery.next()) {
			iNumRegsQuery++;
			Builder bPoint = Point.measurement(sMsmtName);
			long lTimeNs = System.currentTimeMillis();
			bPoint.time(lTimeNs, TimeUnit.MILLISECONDS);
			bPoint.tag("DbId", this.xmlSourceDatabase.getId());
			bPoint.tag("QueryId", xmlQuery.getId());
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
					if (sType.equalsIgnoreCase("tag")) {
						sTypeInflux = "";
					}
					String sFieldValue = rsQuery.getString(sFieldName);
					if (sFieldValue != null) {
						switch (sTypeInflux) {
							case "": //case tag
								bPoint.tag(sTagInflux, sFieldValue);
								break;
							case "long":
								bPoint.addField(sFieldInflux, rsQuery.getLong(sFieldName));
								bPointHasFields = true;
								break;
							case "double":
								bPoint.addField(sFieldInflux, rsQuery.getDouble(sFieldName));
								bPointHasFields = true;
								break;
							case "boolean":
								bPoint.addField(sFieldInflux, rsQuery.getBoolean(sFieldName));
								bPointHasFields = true;
								break;
							case "string":
								bPoint.addField(sFieldInflux, sFieldValue);
								bPointHasFields = true;
								break;
							default: //case "others"
								logger.warn(". getBatchPoints. Field " + sFieldInflux + " not added to: " + sMsmtName + ". " 
										+ sTypeInflux + " not supported in types_influx mapping parameters.");
								break;
						}
					}
				}
			}
			if (bPointHasFields) {
				iNumBatchPoints++;
				Point point = bPoint.build();
				batchPoints.point(point);
			}
		}
		long lSpentTime = System.currentTimeMillis() - lInitTime;
		logger.info(". End getBatchPoints. Measurement: " + sMsmtName + ". " 
				+ iNumRegsQuery + " records readed from Source DB. " 
				+ iNumBatchPoints + " batchPoints to be written into influx."
				+ " Time spent (ms):" + lSpentTime);
		logger.debug(". End getBatchPoints. " + iNumBatchPoints + " batchPoints to write: " + batchPoints.toString());
		if (iNumBatchPoints == 0) {
			logger.warn(". End getBatchPoints. NO batchPoints to write to: " + sMsmtName);
			batchPoints = null;
		}
    	return batchPoints;
    }
    
}
