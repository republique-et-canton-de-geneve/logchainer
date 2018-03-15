package ch.ge.cti.logchainer;  

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ge.cti.logchainer.service.LogWatcherService;

public class LogChainer implements Runnable {
    private static LogWatcherService watcher;
    
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogChainer.class.getName());

    public static void main(String[] args) throws IOException {
	LOG.info("enter main");

	start();
    }

    private static void start() throws IOException {
	LOG.info("enter start");
	
	watcher = new LogWatcherService();
	
	
	new Thread(new LogChainer()).start();

	LOG.info("new thread created");
    }

    @Override 
    public void run() {
	LOG.info("run started");
	
	try {
	    watcher.processEvents();
	} catch (IOException e) {
	    
	    LOG.error("Exception was cached", e);
	}
	
    }
}
