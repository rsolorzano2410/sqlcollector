# sqlcollector
SQLCollector is an agent metric collector. It collects information about the database performance and saves it into InfluxDB.

For the first version SQLCollector gets the performance of one or several Oracle databases and saves this information into one or several Influx databases.

You must configure the connection for source and destination databases and the queries to do in source databases to get the performance information.

It works as a daemon and the frequency used for doing the queries must be configured.

Different logFiles and logLevels can be specified for each source database and also for self monitoring.

The configuration must be done into an xml file.

If you want a different name and/or path for this file than resources/conf/SQLCollector.xml it can be specified with the system parameter sqlcollector.configurationFile (it can be passed in with -Dsqlcollector.configurationFile when calling jar from command line).
