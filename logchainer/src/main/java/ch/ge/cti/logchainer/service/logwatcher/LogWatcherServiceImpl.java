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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;
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
    FluxService fluxService;
    @Autowired
    FileService fileService;

    static final int CONVERT_HOUR_TO_SECONDS = 3600;
    static final int CONVERT_MINUTE_TO_SECONDS = 60;

    private static final String CORRUPTED_FLUXNAME = "corrupted";

    static ArrayList<Client> clients = new ArrayList<>();

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogWatcherServiceImpl.class.getName());

    @Override
    public void initializeFileWatcherByClient(LogChainerConf clientConfList) {
	// keep track of the client number
	int clientNb = 0;
	for (ClientConf client : clientConfList.getListeClientConf()) {
	    // create an object Client from each configuration
	    clients.add(new Client(client));
	    LOG.info("client {} added to the client list", client.getClientId());

	    // register a key for each client
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
	// Iterate over all client for each iteration of the infinity loop
	for (Client client : clients) {
	    // register events (wether anything happened or not)
	    WatchKey watchKey = client.getWatcher().poll();

	    // Launch the treatment only if a file was detected
	    // No use of the take method because we don't want to wait until
	    // an event is detected under one client
	    // to move to the next one
	    if (watchKey != null) {
		LOG.info("event detected on client {}", client.getConf().getClientId());

		client.setKey(watchKey);
		List<WatchedFile> corruptedFiles = clientService.registerEvent(client);

		if (corruptedFiles != null) {
		    corruptedFiles.stream().forEach(new Consumer<WatchedFile>() {
			@Override
			public void accept(WatchedFile corruptedFile) {
			    LOG.info("file {} has invalid name", corruptedFile.getFilename());
			    client.getWatchedFilesByFlux().putIfAbsent(CORRUPTED_FLUXNAME, new ArrayList<>());
			    client.getWatchedFilesByFlux().get(CORRUPTED_FLUXNAME).add(corruptedFile);
			    client.getWatchedFiles().add(corruptedFile);
			    corruptedFile.setRegistered(true);
			}
		    });
		}

		// reset the key to be able to use it again
		if (!client.getKey().reset())
		    throw new CorruptedKeyException(client.getConf().getClientId());
	    }
	    waitingForFileToBeReadyToBeLaunched(client);
	}
    }

    /**
     * Support of the file from it's detection until it is ready to be
     * treated.
     * 
     * @param client
     */
    private void waitingForFileToBeReadyToBeLaunched(Client client) {
	// look at each detected files per client
	for (WatchedFile file : client.getWatchedFiles()) {
	    // present time
	    int actualTime = LocalDateTime.now().getHour() * CONVERT_HOUR_TO_SECONDS
		    + LocalDateTime.now().getMinute() * CONVERT_MINUTE_TO_SECONDS + LocalDateTime.now().getSecond();

	    // registration of the file
	    if (!file.isRegistered())
		fileService.registerFile(client, file);

	    // check the waited delay from the arrived time of the file until
	    // now
	    if (file.getArrivingTime() + DELAY_TRANSFER_FILE < actualTime) {
		LOG.debug("enough time waited for file {}", file.getFilename());
		file.setReadyToBeProcessed(true);
	    }
	}

	// register all the treated files into a list
	ArrayList<String> allDoneFlux = new ArrayList<>();
	// iterate over all the flux
	for (Map.Entry<String, ArrayList<WatchedFile>> flux : client.getWatchedFilesByFlux().entrySet()) {
	    // check if the file can be treated
	    if (fluxService.isFluxReadyToBeTreated(flux)) {
		// flux treatment
		LOG.info("flux {} process starting", flux.getKey());
		if (flux.getKey().equals(CORRUPTED_FLUXNAME)) {
		    fluxService.corruptedFluxProcess(client, allDoneFlux, flux);
		    LOG.info("treatment of flux {} completed", flux.getKey());
		} else {
		    fluxService.fluxTreatment(client, allDoneFlux, flux);
		    LOG.info("treatment of flux {} completed", flux.getKey());
		}
	    }
	}
	// once all files in a flux have been treated, delete the flux in the
	// map
	clientService.removeAllProcessedFluxesFromMap(allDoneFlux, client);
    }

    @Override
    public boolean treatmentAfterDetectionOfEvent(Client client, String filename, WatchedFile file) {
	LOG.debug("start of the file treatment");
	// handle the overflow situation
	if (file.getKind() == OVERFLOW) {
	    LOG.debug("overflow detected");
	}

	// handle a file creation case
	if (file.getKind() == ENTRY_CREATE) {
	    // launch the file treatment
	    fileService.newFileTreatment(client, filename);
	}

	// handle the file situation once it's treatment is done
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
    private boolean reset(Client client, WatchedFile file) {
	// check if the file can be removed (removes it if able)
	if (client.getWatchedFiles().remove(file)) {
	    LOG.debug("file references successfully removed from map");
	    return true;
	} else {
	    LOG.debug("could not delete the file from list");
	    return false;
	}
    }

    /**
     * Getter for the client list which can't modify the list used for the
     * process.
     * 
     * @return the client list
     */
    public static List<Client> getClients() {
	return new ArrayList<>(clients);
    }
}
