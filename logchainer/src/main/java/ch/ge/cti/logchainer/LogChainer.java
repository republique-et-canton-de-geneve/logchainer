package ch.ge.cti.logchainer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ch.ge.cti.logchainer.service.logWatcherService.LogWatcherService;

@SpringBootApplication
public class LogChainer implements CommandLineRunner {
    @Autowired
    private LogWatcherService watcher;

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

	SpringApplication app = new SpringApplication(LogChainer.class);
	app.run(args);
    }

    @Override
    public void run(String... arg0) throws Exception {
	LOG.info("run started");
	try {
	    watcher.processEvents();
	} catch (IOException e) {

	    LOG.error("Exception was cached", e);
	}
    }
}
