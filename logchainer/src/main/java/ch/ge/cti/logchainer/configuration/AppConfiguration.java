package ch.ge.cti.logchainer.configuration;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import ch.ge.cti.logchainer.service.logWatcherService.LogWatcherService;
import ch.ge.cti.logchainer.service.logWatcherService.LogWatcherServiceImpl;

@Configuration
@PropertySource(value = "file:${application.properties}")
@ComponentScan("ch.ge.cti.logchainer")
public class AppConfiguration {

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AppConfiguration.class.getName());

 
    @Bean
    public static LogWatcherService logWatcherService() throws IOException {
	return new LogWatcherServiceImpl();
    }
    

    // @Value("${inputDirectory}")
    // private String inputDir;
    //
    // @Value("${tmpDirectory}")
    // private String tmpDir;
    //
    // @Value("${outputDirectory}")
    // private String outputDir;
    //
    //
    // public String gettmpDir() {
    // return tmpDir;
    // }
    //
    // public String getoutputDir() {
    // return outputDir;
    // }

    // /**
    // * Responsible for the download of the properties file.
    // *
    // * @return the properties as a Properties object
    // * @throws IOException
    // * @throws FileNotFoundException
    // */
    // public static Properties load() throws IOException {
    // Properties properties = new Properties();
    //
    // FileInputStream input = new
    // FileInputStream(LogChainerConstante.FILENAME);
    // LOG.info("input stream opened");
    //
    // try {
    // properties.load(input);
    //
    // LOG.info("properties file found and accessed");
    //
    // return properties;
    //
    // } catch (FileNotFoundException e) {
    // LOG.error("properties file not found", e);
    //
    // throw e;
    //
    // } catch (IOException e) {
    // LOG.error("couldn't access properties file", e);
    // throw e;
    // }
    //
    // finally {
    // input.close();
    // LOG.info("input stream closed");
    // }
    //
    // }
    //
    // /**
    // * Getter for the property.
    // *
    // * @return property name as a String
    // * @throws IOException
    // */
    // public static String getProperty(String key) throws IOException {
    // String property;
    //
    // try {
    // property = AppConfiguration.load().getProperty(key);
    //
    // LOG.info("tmp property correctly accessed");
    //
    // return property;
    //
    // } catch (IOException e) {
    // LOG.error("tmp property not found", e);
    //
    // throw e;
    // }
    //
    // }
}
