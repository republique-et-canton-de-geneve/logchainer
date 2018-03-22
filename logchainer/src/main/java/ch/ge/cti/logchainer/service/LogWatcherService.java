package ch.ge.cti.logchainer.service;

import java.io.IOException;

public interface LogWatcherService {
    /**
     * Infinity loop checking for updates in the directoy and then does the file
     * treatment by calling all necessary methods correctly.
     * 
     * @throws IOException
     */
    void processEvents() throws IOException;
}
