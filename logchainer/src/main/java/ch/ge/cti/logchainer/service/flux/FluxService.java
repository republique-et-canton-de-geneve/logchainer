package ch.ge.cti.logchainer.service.flux;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;

public interface FluxService {
    /**
     * Add a flux to the flux list of the client.
     * 
     * @param fluxname
     * @param client
     */
    void addFlux(String fluxname, Client client);
    
    /**
     * Check if given flux is already registered.
     * 
     * @param fluxname
     * @param client
     * @return
     */
    boolean isNewFlux(String fluxname, Client client);
    
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
    void addFileToFlux(String fluxname, FileWatched clientInfos, Client client);
    
    /**
     * Get the flux name from a file.
     * 
     * @param filename
     * @return fluxname
     */
    String getFluxName(String filename, String separator);
    
    /**
     * Get the stamp used to sort files.
     * 
     * @param filename
     * @param separator
     * @return stamp
     */
    String getSortingStamp(String filename, String separator);
}
