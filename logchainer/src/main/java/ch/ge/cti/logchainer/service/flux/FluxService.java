package ch.ge.cti.logchainer.service.flux;

import java.nio.file.Path;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;

public interface FluxService {
    void addFlux(String fluxname, Client client);
    
    boolean isNewFlux(String fluxname, Client client);
    
    boolean removeFlux(String fluxname, Client client);
    
    void addFileToFlux(String fluxname, FileWatched clientInfos, Client client);
    
    String getFluxName(Path filename, String separator);
    
    String getSortingStamp(String filename, String separator);
}
