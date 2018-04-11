package ch.ge.cti.logchainer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ch.ge.cti.logchainer.generate.LogChainerConf;
import ch.ge.cti.logchainer.generate.ObjectFactory;
import ch.ge.cti.logchainer.service.logwatcher.LogWatcherService;

@SpringBootApplication
public class LogChainer implements CommandLineRunner {
    @Value("${xmlDirectoriesConf}")
    private String xmlDirectoriesConf;
    
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
	
	LOG.debug("LogWatcherServiceImpl initialization started");
	LogChainerConf clientConfList = new LogChainerConf();
	// Accessing the client list provided by the user
	try {
	    clientConfList = loadConfiguration();
	    LOG.info("--------------------- client list accessed");
	} catch (JAXBException e) {
	    LOG.error("Exception while accessing configurations ", e);
	    throw e;
	}

	// Registering all clients as Client objects in a list
	watcher.initializeFileWatcherByClient(clientConfList);
	LOG.debug("LogWatcherServiceImpl initialization completed");

	// infinity loop to actualize endlessly the search for new files
	LOG.debug("start of the infinity loop");
	while (true) {
	    watcher.processEvents();
	}
    }
    
    /**
     * To access the directories' configurations.
     * 
     * @return
     * @throws JAXBException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private LogChainerConf loadConfiguration() throws JAXBException, IOException {
	LOG.debug("starting to read conf file");

	JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
	Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

	LogChainerConf logchainerConf = new LogChainerConf();

	// accessing the xml conf file
	try (FileInputStream xmlFileStream = new FileInputStream(xmlDirectoriesConf)) {
	    logchainerConf = (LogChainerConf) unmarshaller.unmarshal(xmlFileStream);
	}
	LOG.debug("conf file correctly accessed");

	return logchainerConf;
    }
}
