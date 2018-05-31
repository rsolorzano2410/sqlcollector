# sqlcollector
SQLCollector is an agent metric collector. It collects information about the database performance and saves it into InfluxDB.

For the first version SQLCollector gets the performance of one or several Oracle databases and saves this information into one or several Influx databases.

You must configure the connection for source and destination databases and the queries to do in source databases to get the performance information.

It works as a daemon and the frequency used for doing the queries must be configured.

Different logFiles and logLevels can be specified for each source database and also for self monitoring.

The configuration must be done into an xml file.

If you want a different name and/or path for this file than resources/conf/SQLCollector.xml it can be specified with the system parameter sqlcollector.configurationFile (it can be passed in with -Dsqlcollector.configurationFile when calling jar from command line).

## Docker
You can get a docker with the last version from docker store.

### Get docker

```bash
docker pull rsolorzano2410/sqlcollector
```

### Using this image
This image has been built FROM anapsix/alpine-java (an AlpineLinux with a glibc-2.27-r0 and Oracle Java 8) including the executable jar file SQLCollector.jar which is the agent metrics collector.

#### Running the container
First you must get the configuration file and modify it with your settings.

You can get the configuration file with this command:
```bash
docker run -it --rm rsolorzano2410/sqlcollector cat /opt/sqlcollector/conf/SQLCollector.xml > SQLCollector.xml
```

This file must be modified with your settings and copied into the path you prefer.

Then you can start the container with this command:
```bash
docker run --name $CONTAINER_NAME \
			-v $CONTAINER_VOL_LOG:/opt/sqlcollector/log \
			-v $CONTAINER_VOL_CONF:/opt/sqlcollector/conf \
			-d rsolorzano2410/sqlcollector
```

Modify $CONTAINER_NAME with the name you want to assign to the container.

Modify $CONTAINER_VOL_LOG and $CONTAINER_VOL_CONF with the directory where you want to store the log files and the configuration file, respectively.
