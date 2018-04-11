package ch.ge.cti.logchainer.service.flux;

import java.nio.file.Path;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;
import ch.ge.cti.logchainer.exception.LogChainerException;

public class FluxServiceImpl implements FluxService {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FluxServiceImpl.class.getName());
    
    @Override
    public void addFlux(String fluxname, Client client) {
	client.getFluxFileMap().put(fluxname, new ArrayList<FileWatched>());
    }
    
    @Override
    public boolean isNewFlux(String fluxname, Client client) {
	return !client.getFluxFileMap().containsKey(fluxname);
    }
    
    @Override
    public boolean removeFlux(String fluxname, Client client) {
	return client.getFluxFileMap().remove(fluxname) != null; 
    }
    
    @Override
    public void addFileToFlux(String fluxname, FileWatched clientInfos, Client client) {
	client.getFluxFileMap().get(fluxname).add(clientInfos);
    }
    
    /**
     * To get the flux name from a file
     * 
     * @param filename
     * @return fluxname
     * @throws LogChainerException
     */
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
	LOG.debug("the flux of the file is : {}", fluxNameTmp.toString());

	return fluxNameTmp.toString();
    }

    @Override
    public String getSortingStamp(String filename, String separator) {
	String[] nameComponents = filename.split(separator);
	String[] nameStampComponents = nameComponents[nameComponents.length - 1].split("\\.");

	return nameStampComponents[0];
    }
}
