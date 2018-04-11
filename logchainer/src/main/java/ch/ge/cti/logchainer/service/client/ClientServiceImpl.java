package ch.ge.cti.logchainer.service.client;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;
import ch.ge.cti.logchainer.service.flux.FluxService;

@Service
public class ClientServiceImpl implements ClientService {
    @Autowired
    private FluxService fluxActor;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClientServiceImpl.class.getName());

    @Override
    @SuppressWarnings("unchecked")
    public void registerEvent(Client client) {
	// iterating on all events in the key
	for (WatchEvent<?> event : client.getKey().pollEvents()) {
	    FileWatched fileToRegister = new FileWatched(((WatchEvent<Path>) event).context().toString());
	    boolean toRegister = true;

	    // checking if the file has already been registered
	    for (FileWatched file : client.getFilesWatched()) {
		if (file.getFilename().equals(fileToRegister.getFilename())) {
		    toRegister = false;
		    LOG.debug("file already registered");
		}
	    }

	    // registering the file and instantiating it
	    if (toRegister) {
		fileToRegister.setKind(event.kind());
		fileToRegister.setRegistered(false);
		client.getFilesWatched().add(fileToRegister);
		LOG.debug("file registered");
	    }
	}
	// reseting the to be able to use it again
	if (!client.getKey().reset()) {
	    LOG.error("client directory inaccessible or key corrupted");
	    // TODO
	}
    }

    @Override
    public void deleteAllTreatedFluxFromMap(ArrayList<String> allDoneFlux, Client client) {
	// removing the flux one by one
	for (String fluxname : allDoneFlux) {
	    if (fluxActor.removeFlux(fluxname, client)) {
		LOG.debug("flux {} has been removed from the map", fluxname);
	    } else {
		// TODO
		LOG.error("could not delete flux from map");
	    }
	}
    }
}
