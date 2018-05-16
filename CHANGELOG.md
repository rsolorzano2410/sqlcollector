# v 0.4 (2018-05-16)
### New Features
* Different logFiles and logLevels can be specified for each source database and also for self monitoring.
* The log configuration is specified into SQLCollector.xml (log4j2.xml is not used anymore).
* New system parameter sqlcollector.configurationFile with the path for configuration file (it can be passed with -Dsqlcollector.configurationFile when calling jar from command line).

### fixes

### breaking changes
* log4j2.xml is not used anymore.

# v 0.3 (2018-05-08)
### New Features
* Self monitoring included. Information about the performance of the process saved into InfluxDB with the measurement SelfMonStats.

### fixes

### breaking changes

# v 0.2 (2018-05-02)
### New Features
* Where conditions for the queries accepting parameters.

### fixes

### breaking changes

# v 0.1 (2018-04-25)
### New Features
* Initial version.
* Configuration file must contain source databases information, destination databases information, measurements information and queries information.
* The where conditions for the queries must be hardcoded (without parameters).
* The logging configuration must be specified with the log4j2.xml file.

### fixes

### breaking changes
