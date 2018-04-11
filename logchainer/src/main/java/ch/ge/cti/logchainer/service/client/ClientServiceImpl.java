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
public class ClientServiceImpl implements ClientService{
    @Autowired
    private FluxService fluxActor;
    
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClientServiceImpl.class.getName());

    @Override
    @SuppressWarnings("unchecked")
    public void registerEvent(Client client) {
	for (WatchEvent<?> event : client.getKey().pollEvents()) {
	    FileWatched clientInfos = new FileWatched(((WatchEvent<Path>) event).context().toString());
	    boolean toRegister = true;

	    for (FileWatched info : client.getFilesWatched()) {
		if (info.getFilename().equals(clientInfos.getFilename())) {
		    toRegister = false;
		    LOG.debug("file already registered");
		}
	    }

	    if (toRegister) {
		clientInfos.setKind(event.kind());
		clientInfos.setRegistered(false);
		client.getFilesWatched().add(clientInfos);
		LOG.debug("file registered");
	    }
	}

	client.getKey().reset();
    }
    
    @Override
    public void deleteAllTreatedFluxFromMap(ArrayList<String> allDoneFlux, Client client) {
	for (String fluxname : allDoneFlux) {
	    if(fluxActor.removeFlux(fluxname, client)) {
		LOG.debug("flux {} has been removed from the map", fluxname);
	    } else {
		//TODO
		LOG.error("could not delete flux from map");
	    }
	}
    }
}
