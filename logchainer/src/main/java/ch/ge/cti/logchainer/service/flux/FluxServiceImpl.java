package ch.ge.cti.logchainer.service.flux;

import java.nio.file.Path;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;

public class FluxServiceImpl implements FluxService {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FluxServiceImpl.class.getName());
    
    @Override
    public void addFlux(String fluxname, Client client) {
	LOG.debug("adding flux {} to client {}", fluxname, client.getConf().getClientId());
	client.getFluxFileMap().put(fluxname, new ArrayList<FileWatched>());
    }
    
    @Override
    public boolean isNewFlux(String fluxname, Client client) {
	LOG.debug("flux {} is detected as a new flux", fluxname);
	return !client.getFluxFileMap().containsKey(fluxname);
    }
    
    @Override
    public boolean removeFlux(String fluxname, Client client) {
	LOG.debug("removing flux {} from list", fluxname);
	return client.getFluxFileMap().remove(fluxname) != null; 
    }
    
    @Override
    public void addFileToFlux(String fluxname, FileWatched file, Client client) {
	LOG.debug("mapping file {} to the flux {}", file.getFilename(), fluxname);
	client.getFluxFileMap().get(fluxname).add(file);
    }
    
    @Override
    public String getFluxName(Path filename, String separator) {
	LOG.debug("getting flux name method entered");
	// if (!filename.toString().contains("_"))
	// throw new LogChainerException("incorrect filename");

	StringBuilder fluxNameTmp = new StringBuilder();

	// finding the flux name of the file, knowing each flux is situated at
	// the beginning of the filename (before the separator)
	String[] nameComponents = filename.toString().split(separator);
	for (int i = 0; i < nameComponents.length - 1; ++i) {
	    fluxNameTmp.append(nameComponents[i]);
	}
	LOG.debug("the flux of the file {} is : {}", filename, fluxNameTmp.toString());

	return fluxNameTmp.toString();
    }

    @Override
    public String getSortingStamp(String filename, String separator) {
	LOG.debug("getting stamp method entered");
	String[] nameComponents = filename.split(separator);
	String[] nameStampComponents = nameComponents[nameComponents.length - 1].split("\\.");
	
	LOG.debug("the stamp of the file {} is : {}", filename, nameStampComponents[0]);
	return nameStampComponents[0];
    }
}
