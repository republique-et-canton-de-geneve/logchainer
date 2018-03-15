package ch.ge.cti.logchainer;  

import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ge.cti.logchainer.service.LogWatcherService;

public class LogChainer implements Runnable {
    private static LogWatcherService WATCHER;
    
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogChainer.class.getName());

    public static void main(String[] args) throws IOException {
	LOG.info("enter main");

	start(args);
    }

    private static void start(String[] args) throws IOException {
	LOG.info("enter start");
	
	WATCHER = new LogWatcherService();
	
	
	new Thread(new LogChainer()).start();

	LOG.info("new thread created");
    }

    @Override 
    public void run() {
	LOG.info("run started");
	
	try {
	    WATCHER.processEvents();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
    }
}
