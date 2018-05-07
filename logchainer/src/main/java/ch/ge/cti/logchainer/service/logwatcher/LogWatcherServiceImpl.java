package ch.ge.cti.logchainer.service.logwatcher;

import static ch.ge.cti.logchainer.constant.LogChainerConstant.DELAY_TRANSFER_FILE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;
import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.exception.CorruptedKeyException;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.LogChainerConf;
import ch.ge.cti.logchainer.service.client.ClientService;
import ch.ge.cti.logchainer.service.file.FileService;
import ch.ge.cti.logchainer.service.flux.FluxService;
import ch.ge.cti.logchainer.service.folder.FolderService;

@Service
public class LogWatcherServiceImpl implements LogWatcherService {
    @Autowired
    FolderService mover;
    @Autowired
    ClientService clientService;
    @Autowired
    private FluxService fluxService;
    @Autowired
    FileService fileService;

    static final int CONVERT_HOUR_TO_SECONDS = 3600;
    static final int CONVERT_MINUTE_TO_SECONDS = 60;

    private final String corruptedFluxName = "corrupted";

    ArrayList<Client> clients = new ArrayList<>();

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogWatcherServiceImpl.class.getName());

    @Override
    public void initializeFileWatcherByClient(LogChainerConf clientConfList) {
	// keeping track of the client number
	int clientNb = 0;
	for (ClientConf client : clientConfList.getListeClientConf()) {
	    // creating an object Client from each configuration
	    clients.add(new Client(client));
	    LOG.info("client {} added to the client list", client.getClientId());

	    // registering a key for each client
	    try {
		Path inputDirPath = Paths.get(clients.get(clientNb).getConf().getInputDir());
		WatchService watcher = clients.get(clientNb).getWatcher();

		clients.get(clientNb).setKey(inputDirPath.register(watcher, ENTRY_CREATE));
		LOG.debug("key created as an ENTRY_CREATE");
	    } catch (IOException e) {
		throw new BusinessException("couldn't complete the initialization", e);
	    }
	    clientNb++;
	}
    }

    @Override
    public void processEvents() {
	// Iterating over all client for each iteration of the infinity loop
	for (Client client : clients) {
	    // registering events (wether anything happened or not)
	    WatchKey watchKey = client.getWatcher().poll();

	    // Launching the treatment only if a file was detected
	    // No use of the take method because we don't want to wait until
	    // an event is detected under one client
	    // to move to the next one
	    if (watchKey != null) {
		LOG.info("event detected on client {}", client.getConf().getClientId());

		client.setKey(watchKey);
		FileWatched corruptedFile = clientService.registerEvent(client);

		if (corruptedFile != null) {
		    LOG.info("file {} has invalid name", corruptedFile.getFilename());
		    client.getFluxFileMap().putIfAbsent(corruptedFluxName, new ArrayList<>());
		    client.getFluxFileMap().get(corruptedFluxName).add(corruptedFile);
		    client.getFilesWatched().add(corruptedFile);
		    corruptedFile.setRegistered(true);
		}

		// reseting the key to be able to use it again
		if (!client.getKey().reset())
		    throw new CorruptedKeyException(client.getConf().getClientId());
	    }
	    waitingForFileToBeReadyToBeLaunched(client);
	}
    }

    /**
     * Take in charge of the file from it's detection until it is ready to be
     * treated.
     * 
     * @param client
     */
    private void waitingForFileToBeReadyToBeLaunched(Client client) {
	// looking at each detected files per client
	for (FileWatched file : client.getFilesWatched()) {
	    // present time
	    int actualTime = LocalDateTime.now().getHour() * CONVERT_HOUR_TO_SECONDS
		    + LocalDateTime.now().getMinute() * CONVERT_MINUTE_TO_SECONDS + LocalDateTime.now().getSecond();

	    // registration of the file
	    if (!file.isRegistered())
		fileService.registerFile(client, file);

	    // checking the waited delay from the arrived time of the file until
	    // now
	    if (file.getArrivingTime() + DELAY_TRANSFER_FILE < actualTime) {
		LOG.debug("enough time waited for file {}", file.getFilename());
		file.setReadyToBeTreated(true);
	    }
	}

	// registering all the treated files into a list
	ArrayList<String> allDoneFlux = new ArrayList<>();
	// iterating over all the flux
	for (Map.Entry<String, ArrayList<FileWatched>> flux : client.getFluxFileMap().entrySet()) {
	    // checking if the file can be treated
	    if (fluxService.isFluxReadyToBeTreated(flux)) {
		// flux treatment
		LOG.info("flux {} process starting", flux.getKey());
		if (flux.getKey().equals(corruptedFluxName)) {
		    fluxService.corruptedFluxProcess(client, allDoneFlux, flux);
		    LOG.info("treatment of flux {} completed", flux.getKey());
		} else {
		    fluxService.fluxTreatment(client, allDoneFlux, flux);
		    LOG.info("treatment of flux {} completed", flux.getKey());
		}
	    }
	}
	// once all files in a flux have been treated, deleting the flux in the
	// map
	clientService.deleteAllTreatedFluxFromMap(allDoneFlux, client);
    }

    @Override
    public boolean treatmentAfterDetectionOfEvent(Client client, String filename, FileWatched file) {
	LOG.debug("start of the file treatment");
	// handling the overflow situation
	if (file.getKind() == OVERFLOW) {
	    LOG.debug("overflow detected");
	}

	// handling a file creation case
	if (file.getKind() == ENTRY_CREATE) {
	    // launching the file treatment
	    fileService.newFileTreatment(client, filename);
	}

	// handling the file situation once it's treatment is done
	if (reset(client, file)) {
	    LOG.info("file {} treated", filename);
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Remove the file of the file list once it has been treated.
     * 
     * @param clientNb
     * @return validity of the key
     */
    private boolean reset(Client client, FileWatched file) {
	// checking if the file can be removed (removes it if able)
	if (client.getFilesWatched().remove(file)) {
	    LOG.debug("file references successfully removed from map");
	    return true;
	} else {
	    LOG.debug("could not delete the file from list");
	    return false;
	}
    }
}
