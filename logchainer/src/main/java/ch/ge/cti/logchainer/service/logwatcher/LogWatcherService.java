package ch.ge.cti.logchainer.service.logwatcher;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import ch.ge.cti.logchainer.generate.LogChainerConf;

public interface LogWatcherService {
    /**
     * Create a list where all clients are registered after being instantiated
     * as Client objects. Initialize their WatchKey.
     * 
     * @param clientConfList
     * @throws IOException
     */
    void initializeFileWatcherByClient(LogChainerConf clientConfList) throws IOException;
    
    /**
     * Infinity loop checking for updates in the directoy and then does the file
     * treatment by calling all necessary methods correctly.
     * 
     * @throws IOException
     */
    void processEvents() throws IOException, JAXBException;
}
