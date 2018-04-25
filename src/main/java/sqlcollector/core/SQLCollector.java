package sqlcollector.core;

import sqlcollector.core.logs.L4j;
import sqlcollector.core.threads.ThreadManager;
import sqlcollector.utils.constants.Constants;

import java.io.File;

public class SQLCollector {
    public static void main(String[] args) {
        File f = new File(".");
        System.out.println(f.getAbsolutePath());
        System.setProperty("log4j.configurationFile", Constants.L4J2_XML);
        L4j.getL4j().info("############################");
    	L4j.getL4j().info("# Oracle metrics collector #");
        L4j.getL4j().info("#       SQLCollector       #");
        L4j.getL4j().info("#          v.0.1           #");
        L4j.getL4j().info("#         24-04-18         #");
        L4j.getL4j().info("############################");
        L4j.getL4j().info("Loading settings: SQLCollector.xml");
        
        ThreadManager threadManager = new ThreadManager();
        threadManager.run();
    }
}
