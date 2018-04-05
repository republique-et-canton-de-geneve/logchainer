package ch.ge.cti.logchainer.service.logwatcher;

import java.io.IOException;

import javax.xml.bind.JAXBException;

@FunctionalInterface
public interface LogWatcherService {
    /**
     * Infinity loop checking for updates in the directoy and then does the file
     * treatment by calling all necessary methods correctly.
     * 
     * @throws IOException
     */
    void processEvents() throws IOException, JAXBException;

}
