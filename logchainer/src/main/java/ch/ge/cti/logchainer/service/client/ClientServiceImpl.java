package ch.ge.cti.logchainer.service.client;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;
import ch.ge.cti.logchainer.service.flux.FluxService;
import ch.ge.cti.logchainer.service.helper.FileHelper;

@Service
public class ClientServiceImpl implements ClientService {
    FileHelper fileHelper = new FileHelper();

    @Autowired
    FluxService fluxService;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClientServiceImpl.class.getName());

    @Override
    @SuppressWarnings("unchecked")
    public List<WatchedFile> registerEvent(Client client) {
	List<WatchedFile> corruptedFiles = new ArrayList<>();

	// iterating on all events in the key
	for (WatchEvent<?> event : client.getKey().pollEvents()) {
	    WatchedFile fileToRegister = new WatchedFile(((WatchEvent<Path>) event).context().toString());
	    boolean toRegister = true;

	    if (LOG.isDebugEnabled())
		LOG.debug("Treating event from file : {}", fileToRegister.getFilename());

	    // checking the validity of the filename
	    if (!fileToRegister.getFilename().contains(fileHelper.getSeparator(client)))
		corruptedFiles.add(fileToRegister);

	    // checking if the file has already been registered
	    for (WatchedFile file : client.getWatchedFiles()) {
		if (file.getFilename().equals(fileToRegister.getFilename())) {
		    toRegister = false;
		    LOG.debug("file already registered");
		}
	    }

	    // registering the file and instantiating it
	    if (toRegister) {
		fileToRegister.setKind(event.kind());
		fileToRegister.setRegistered(false);
		client.getWatchedFiles().add(fileToRegister);
		LOG.debug("file registered");
	    }
	}

	return corruptedFiles.isEmpty() ? null : corruptedFiles;
    }

    @Override
    public void removeAllProcessedFluxesFromMap(List<String> allDoneFlux, Client client) {
	// removing the flux one by one
	for (String fluxname : allDoneFlux) {
	    client.getWatchedFiles().removeAll(client.getWatchedFilesByFlux().get(fluxname));
	    if (fluxService.removeFlux(fluxname, client)) {
		LOG.debug("flux {} has been removed from the map", fluxname);
	    } else {
		LOG.error("could not delete flux from map");
	    }
	}
    }
}
