# JMXMonitoring

This application is a simple JMX monitoring logger.

##Usage:

We need a JMX URL and interval of monitoring, the output is written to a log-file (my-log-filename.log) 

first build: mvn install, then

mvn resources:resources -Dapplication="my-log-filename" exec:java -Djmxurl="service:jmx:rmi:///jndi/rmi://127.0.0.1:9020/jmxrmi" -Dinterval="60"


run your java program with the following jmx options, we connect on port 9020:

java 	-Dcom.sun.management.jmxremote
	-Dcom.sun.management.jmxremote.port=9020 
	-Dcom.sun.management.jmxremote.local.only=false
	-Dcom.sun.management.jmxremote.authenticate=false
	-Dcom.sun.management.jmxremote.ssl=false    <Main-class> 


URL: service:jmx:rmi://<TARGET_MACHINE>:<JMX_RMI_SERVER_PORT>/jndi/rmi://<TARGET_MACHINE>:<RMI_REGISTRY_PORT>/jmxrmi

 * Simplified Connect URL: service:jmx:rmi:///jndi/rmi://<TARGET_MACHINE>:<RMI_REGISTRY_PORT>/jmxrmi


##Output to log-file:

2015-12-11 00:33:47 INFO  JMXMonitoring:54 - Using the remote JMX URL :service:jmx:rmi:///jndi/rmi://127.0.0.1:9020/jmxrmi
2015-12-11 00:33:47 INFO  JMXMonitoring:55 - interval :1000
2015-12-11 00:33:48 INFO  JMXMonitoring:98 - Heap(MB): 118 of committed(MB): 1029 ,threads  11 , CPU 0.11
2015-12-11 00:33:49 INFO  JMXMonitoring:98 - Heap(MB): 118 of committed(MB): 1029 ,threads  11 , CPU 0.03
2015-12-11 00:33:50 INFO  JMXMonitoring:98 - Heap(MB): 118 of committed(MB): 1029 ,threads  11 , CPU 0.09
2015-12-11 00:33:51 INFO  JMXMonitoring:98 - Heap(MB): 118 of committed(MB): 1029 ,threads  11 , CPU 0.10
2015-12-11 00:33:52 INFO  JMXMonitoring:98 - Heap(MB): 118 of committed(MB): 1029 ,threads  11 , CPU 0.04
2015-12-11 00:33:53 INFO  JMXMonitoring:98 - Heap(MB): 118 of committed(MB): 1029 ,threads  11 , CPU 0.04


##Note: 

We expect that Java and Maven are installed

In your environment set the following environment:

JAVA_HOME=<your java directory>
MAVEN_HOME=<your maven directory>

Define your path:

Unix: PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:...

Windows: PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;...


