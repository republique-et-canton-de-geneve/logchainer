package ch.ge.cti.logchainer.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ge.cti.logchainer.constante.LogChainerConstante;

public class AppConfiguration {
    
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory
	    .getLogger(AppConfiguration.class.getName());
    
    /**
     * Responsible for the download of the properties file.
     * @return the properties as a Properties object
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static Properties load() throws IOException, FileNotFoundException{
	Properties properties = new Properties();

	FileInputStream input = new FileInputStream(LogChainerConstante.FILENAME);
	LOG.debug("input stream opened");
	
	try{
	    properties.load(input);
	    
	    LOG.debug("properties file found and accessed");
	    
	    return properties;
	    
	} catch (FileNotFoundException e) {
	    LOG.error("properties file not found");
	    
	    return null;
	    
	} catch (IOException e) {
	    LOG.error("couldn't access properties file");
	    return null;
	}
	
	finally{
	    input.close();
	    LOG.debug("input stream closed");
	}

    }
}
