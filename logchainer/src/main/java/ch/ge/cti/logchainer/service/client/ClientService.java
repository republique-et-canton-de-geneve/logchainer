package ch.ge.cti.logchainer.service.client;

import java.util.ArrayList;

import ch.ge.cti.logchainer.beans.Client;

public interface ClientService {
    /**
     * Register a file as a new FileWatched object if it's not already existing
     * and adding it to the client's file list
     * 
     * @param client
     */
    void registerEvent(Client client);

    /**
     * Remove all flux that have been entirely treated from the client's map.
     * 
     * @param allDoneFlux
     * @param client
     */
    void deleteAllTreatedFluxFromMap(ArrayList<String> allDoneFlux, Client client);
}
