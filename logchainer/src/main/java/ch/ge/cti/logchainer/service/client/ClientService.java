package ch.ge.cti.logchainer.service.client;

import java.util.List;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;

public interface ClientService {
    /**
     * Register a file as a new FileWatched object if it's not already existing
     * and adding it to the client's file list
     * 
     * @param client
     * @return the corrupted file or null if there are none
     */
    List<FileWatched> registerEvent(Client client);

    /**
     * Remove all flux that have been entirely treated from the client's map.
     * 
     * @param allDoneFlux
     * @param client
     */
    void deleteAllTreatedFluxFromMap(List<String> allDoneFlux, Client client);
}
