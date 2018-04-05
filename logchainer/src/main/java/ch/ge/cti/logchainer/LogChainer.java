package ch.ge.cti.logchainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ch.ge.cti.logchainer.service.logwatcher.LogWatcherService;

@SpringBootApplication
public class LogChainer implements CommandLineRunner {
    @Autowired
    private LogWatcherService watcher;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogChainer.class.getName());

    public static void main(String[] args) {
	LOG.debug("enter main");

	start(args);
    }

    private static void start(String[] args) {
	LOG.debug("enter start");

	SpringApplication app = new SpringApplication(LogChainer.class);
	// disable the spring banner
	app.setBannerMode(Banner.Mode.OFF);
	app.run(args);
    }

    @Override
    public void run(String... arg0) throws Exception {
	LOG.debug("run started");
	watcher.processEvents();
    }
}
