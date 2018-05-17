package ch.ge.cti.logchainer.service.flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;

public interface FluxService {
    /**
     * Add a flux to the flux list of the client.
     * 
     * @param fluxname
     * @param client
     */
    void addFlux(String fluxname, Client client);

    /**
     * Remove flux from the flux list of the client.
     * 
     * @param fluxname
     * @param client
     * @return
     */
    boolean removeFlux(String fluxname, Client client);

    /**
     * Add a file to a given flux in the map of the client.
     * 
     * @param fluxname
     * @param clientInfos
     * @param client
     */
    void addFileToFlux(String fluxname, WatchedFile clientInfos, Client client);

    /**
     * Get the flux name from a file.
     * 
     * @param filename
     * @return fluxname
     */
    String getFluxName(String filename, String separator, String stampPosition);

    /**
     * Get the stamp used to sort files.
     * 
     * @param filename
     * @param separator
     * @return stamp
     */
    String getSortingStamp(String filename, String separator, String stampPosition);

    /**
     * Check if the flux can be processed.
     * 
     * @param flux
     * @return
     */
    boolean isFluxReadyToBeProcessed(Map.Entry<String, ArrayList<WatchedFile>> flux);

    /**
     * Trigger the flux process.
     * 
     * @param client
     * @param allDoneFlux
     * @param flux
     */
    void fluxProcess(Client client, List<String> allDoneFlux, Map.Entry<String, ArrayList<WatchedFile>> flux);

    /**
     * Trigger a corrupted flux process.
     * 
     * @param client
     * @param allDoneFlux
     * @param flux
     */
    void corruptedFluxProcess(Client client, List<String> allDoneFlux, Map.Entry<String, ArrayList<WatchedFile>> flux);
}
