import com.sun.management.OperatingSystemMXBean;
import javax.management.*;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import org.apache.log4j.Logger;

/**
 * Simple JMX monitoring
 *
 * usage : java JMXMonitoring <JMX-URL> <monitoring-interval-in-seconds>
 *
 *
 * URL: service:jmx:rmi://<TARGET_MACHINE>:<JMX_RMI_SERVER_PORT>/jndi/rmi://<TARGET_MACHINE>:<RMI_REGISTRY_PORT>/jmxrmi
 * Simplified Connect URL: service:jmx:rmi:///jndi/rmi://<TARGET_MACHINE>:<RMI_REGISTRY_PORT>/jmxrmi
 *
 * Example:
 *
 * java service:jmx:rmi:///jndi/rmi://127.0.0.1:9020/jmxrmi 3
 *
 * Output: jmx-application.log
 *
 * Created by oudenaar on 09/12/15.
 */
public class JMXMonitoring {

        private static final String USER_NAME = "";
        private static final String PASSWORD = "";
        private static final Long POLLING_TIME_UNIT = 1000L;// 1 sec
        private static final String HEAP_MEMORY_USAGE = "HeapMemoryUsage";
        private static final String THREAD_COUNT = "ThreadCount";
        private static Long interval = 1L;
        final static Logger logger = Logger.getLogger(JMXMonitoring.class);


        public static void main(String[] args) throws Exception {
            String outputFileName = null;
            String remoteServerJMXUrl = null;

            if (args[0] != null && args[1] != null) {
                remoteServerJMXUrl = args[0];
                interval = Long.parseLong(args[1]);
            } else {
                logger.info("No fileName/Remote JMX URL provided: please see the usage..");
            }
            interval = interval * POLLING_TIME_UNIT;
            logger.info("Using the remote JMX URL :" + remoteServerJMXUrl);
            logger.info("interval :" + interval);
            JMXServiceURL url = new JMXServiceURL(remoteServerJMXUrl);
            Hashtable<String, String[]> environment = new Hashtable<String, String[]>();
            String[] credentials = new String[]{USER_NAME, PASSWORD};
            environment.put(JMXConnector.CREDENTIALS, credentials);
            boolean connected = false; // tracking the connection status
            JMXConnector jmxConnection = null;

            do {
                try {
                    jmxConnection = JMXConnectorFactory.connect(url, environment);
                    connected = true;
                } catch (IOException e) { //re-trying the connection while the remote server become available..o
                    logger.info("Error while connecting to the remote server : re-trying...");
                    Thread.sleep(5000L); // 5 sec sleep
                }
            } while (!connected);

            MBeanServerConnection mBeanServerConnection = jmxConnection.getMBeanServerConnection();
            CPUMonitor performanceMonitor = new CPUMonitor();
            performanceMonitor.init(mBeanServerConnection);

            try {
                while (true) {
                    getDataString(mBeanServerConnection, performanceMonitor);
                }
            } finally {

                jmxConnection.close();
            }
        }

        private static String[] getDataString(MBeanServerConnection mBeanServerConnection, CPUMonitor performanceMonitor) throws Exception {
            String heapMemCommitted = getComplexProperty(mBeanServerConnection, ManagementFactory.MEMORY_MXBEAN_NAME, HEAP_MEMORY_USAGE, "committed").toString();
            String heapMemUsage = getComplexProperty(mBeanServerConnection, ManagementFactory.MEMORY_MXBEAN_NAME, HEAP_MEMORY_USAGE, "used").toString();
            String threadCount = mBeanServerConnection.getAttribute(new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME), THREAD_COUNT).toString();

            Thread.sleep(interval);

            String cpuUsage = String.format("%.2f", performanceMonitor.getCpuUsage(mBeanServerConnection)*100);
            //String systemLoadAverage = mBeanServerConnection.getAttribute(
              //      new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME), SYSTEM_LOAD_AVERAGE).toString();
            String[] tempArray = {heapMemUsage, heapMemCommitted, threadCount, cpuUsage};
            logger.info("Heap(MB): " + heapMemUsage.substring(0,heapMemUsage.length()-6) + " of committed(MB): " + heapMemCommitted.substring(0,heapMemCommitted.length()-6)  + " ,threads  " + threadCount + " , CPU " + cpuUsage);
            return tempArray;
        }

        public static String getComplexProperty(MBeanServerConnection mbsc, String mbeanName, String prop1, String prop2) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException, NullPointerException, IOException {
            CompositeDataSupport ds1 = (CompositeDataSupport) mbsc.getAttribute(
                    new ObjectName(mbeanName), prop1);
            return ds1.get(prop2).toString();
        }
    }

    //Original version generously taken from a Stack-Overflow forum post
    class CPUMonitor {
        private static final String AVAILABLE_PROCESSORS = "AvailableProcessors";
        private static final String PROCESS_CPU_TIME = "ProcessCpuTime";
        private int availableProcessors = 0;
        private long lastSystemTime = 0;
        private long lastProcessCpuTime = 0;

        public void init(MBeanServerConnection mBeanServerConnection) throws Exception {
            availableProcessors = (Integer) mBeanServerConnection.getAttribute(
                    new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME), AVAILABLE_PROCESSORS);
            baselineCounters(mBeanServerConnection);
        }

        public synchronized double getCpuUsage(MBeanServerConnection mBeanServerConnection) throws Exception {
            long systemTime = System.nanoTime();
            long processCpuTime = 0;
            if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean) {
                processCpuTime = (Long) mBeanServerConnection.getAttribute(
                        new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME), PROCESS_CPU_TIME);
            }
            double cpuUsage = (double) (processCpuTime - lastProcessCpuTime) / (systemTime - lastSystemTime);
            lastSystemTime = systemTime;
            lastProcessCpuTime = processCpuTime;
            return cpuUsage / availableProcessors;
        }

        private void baselineCounters(MBeanServerConnection mBeanServerConnection) throws Exception {
            lastSystemTime = System.nanoTime();
            if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean) {
                lastProcessCpuTime = (Long) mBeanServerConnection.getAttribute(
                        new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME), PROCESS_CPU_TIME);
            }
        }
    }

