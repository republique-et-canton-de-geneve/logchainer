package ch.ge.cti.logchainer.service.client;

import java.util.List;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;

public interface ClientService {
    /**
     * Register a file as a new WatchedFile object if it's not already existing
     * and adding it to the client's file list
     * 
     * @param client
     * @return the corrupted file or null if there are none
     */
    List<WatchedFile> registerEvent(Client client);

    /**
     * Remove all flux that have been entirely treated from the client's map.
     * 
     * @param allDoneFlux
     * @param client
     */
    void removeAllProcessedFluxesFromMap(List<String> allDoneFlux, Client client);
}
