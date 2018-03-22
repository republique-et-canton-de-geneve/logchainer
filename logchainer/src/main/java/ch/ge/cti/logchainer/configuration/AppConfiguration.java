package ch.ge.cti.logchainer.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import ch.ge.cti.logchainer.constante.LogChainerConstante;

@Repository
public class AppConfiguration {

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AppConfiguration.class.getName());

    /**
     * Non instantiable class
     */
    private AppConfiguration() {
    }

    /**
     * Responsible for the download of the properties file.
     * 
     * @return the properties as a Properties object
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static Properties load() throws IOException {
	Properties properties = new Properties();

	FileInputStream input = new FileInputStream(LogChainerConstante.FILENAME);
	LOG.debug("input stream opened");

	try {
	    properties.load(input);

	    LOG.debug("properties file found and accessed");

	    return properties;

	} catch (FileNotFoundException e) {
	    LOG.error("properties file not found", e);

	    throw e;

	} catch (IOException e) {
	    LOG.error("couldn't access properties file", e);
	    throw e;
	}

	finally {
	    input.close();
	    LOG.debug("input stream closed");
	}

    }

    /**
     * Getter for the property.
     * 
     * @return property name as a String
     * @throws IOException
     */
    public static String getProperty(String key) throws IOException {
	String property;

	try {
	    property = AppConfiguration.load().getProperty(key);

	    LOG.info("tmp property correctly accessed");

	    return property;

	} catch (IOException e) {
	    LOG.error("tmp property not found", e);

	    throw e;
	}

    }
}
