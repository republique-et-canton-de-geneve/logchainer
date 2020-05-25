/*
 * <Log Chainer>
 *
 * Copyright (C) 2018 R�publique et Canton de Gen�ve
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ch.ge.cti.logchainer.constant.LogChainerConstant;
import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.exception.handler.LogChainerExceptionHandlerServiceImpl;
import ch.ge.cti.logchainer.generate.LogChainerConf;
import ch.ge.cti.logchainer.generate.ObjectFactory;
import ch.ge.cti.logchainer.service.logwatcher.LogWatcherService;

@SpringBootApplication
//@SpringBootTest(classes = ApplicationBootstrap.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LogChainer implements CommandLineRunner {
    @Value("${xmlDirectoriesConf}")
    private String xmlDirectoriesConf;

    @Autowired
    private LogWatcherService watcher;

    @Autowired
    private LogChainerExceptionHandlerServiceImpl exceptionHandler;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogChainer.class.getName());

    public static void main(String[] args) {
	LOG.debug("enter main");

	start(args);
    }

    /**
     * Launch the process.
     * 
     * @param args
     */
    private static void start(String[] args) {
	LOG.debug("enter start");

	SpringApplication app = new SpringApplication(LogChainer.class);
	// disable the spring banner
	app.setBannerMode(Banner.Mode.OFF);
	app.setWebApplicationType(WebApplicationType.NONE);

	app.run(args);
    }

    @Override
    public void run(String... arg0) {
	boolean modeBatch = false;
	LOG.debug("run started");

	LOG.debug("LogWatcherServiceImpl initialization started");
	for (String arg : arg0) {
	    if (arg != null && arg.equalsIgnoreCase(LogChainerConstant.MODE_BATCH)) {
		modeBatch = true;
	    }
	}
	
	LogChainerConf clientConfList = new LogChainerConf();
	// Access the client list provided by the user
	try {
	    clientConfList = loadConfiguration();
	    LOG.info("client list accessed");
	} catch (JAXBException e) {
	    throw new BusinessException(e);
	}

	// Register all clients as Client objects in a list
	watcher.initializeFileWatcherByClient(clientConfList);
	LOG.debug("LogWatcherServiceImpl initialization completed");
	boolean withHisto = true;
	// Solution simple et rapide pour la mise en place de l'option "mode batch"
	if (modeBatch) {
	    try {
		    watcher.processEvents(withHisto);
		    LOG.info("SUCCES - Fin du traitement");
		    System.exit(0);
		} catch (RuntimeException e) {
		    exceptionHandler.handleException(e);
		    LOG.error("ERROR - Fin du traitement");
		    System.exit(-1);
		}
	} else {
	    // infinity loop to actualize endlessly the search for new files
	    LOG.debug("start of the infinity loop");
	    // loop variable controls if the loop continues
	    boolean loop = true;
	    while (loop) {
		try {
		    watcher.processEvents(withHisto);
		} catch (RuntimeException e) {
		    exceptionHandler.handleException(e);
		}
		withHisto = false;
	    }
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
    private LogChainerConf loadConfiguration() throws JAXBException {
	LOG.debug("starting to read conf file");
	LogChainerConf logchainerConf = new LogChainerConf();

	JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
	Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

	// access the xml conf file
	try (FileInputStream xmlFileStream = new FileInputStream(xmlDirectoriesConf)) {
	    logchainerConf = (LogChainerConf) unmarshaller.unmarshal(xmlFileStream);
	} catch (FileNotFoundException e) {
	    throw new BusinessException("xml configuration file not found", e);
	} catch (IOException e) {
	    throw new BusinessException(e);
	}
	LOG.debug("conf file correctly accessed");

	return logchainerConf;
    }
}
