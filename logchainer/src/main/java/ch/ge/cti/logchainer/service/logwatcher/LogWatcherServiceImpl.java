package ch.ge.cti.logchainer.service.logwatcher;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;
import ch.ge.cti.logchainer.constante.LogChainerConstante;
import ch.ge.cti.logchainer.exception.LogChainerException;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.LogChainerConf;
import ch.ge.cti.logchainer.service.client.ClientService;
import ch.ge.cti.logchainer.service.flux.FluxService;
import ch.ge.cti.logchainer.service.folder.FolderService;
import ch.ge.cti.logchainer.service.hash.HashService;
import ch.ge.cti.logchainer.service.logchainer.LogChainerService;
import ch.ge.cti.logchainer.service.properties.UtilsComponents;

@Service
public class LogWatcherServiceImpl implements LogWatcherService {
    @Autowired
    private FolderService mover;
    @Autowired
    private LogChainerService chainer;
    @Autowired
    private HashService hasher;
    @Autowired
    private ClientService clientActor;
    @Autowired
    private FluxService fluxActor;
    @Autowired
    private UtilsComponents component;

    private ArrayList<Client> clients = new ArrayList<>();

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogWatcherServiceImpl.class.getName());

    @Override
    public void initializeFileWatcherByClient(LogChainerConf clientConfList) throws IOException {
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
		LOG.error("couldn't complete the initialization : {}", e.toString(), e);
		throw e;
	    }
	    clientNb++;
	}
    }

    @Override
    public void processEvents() throws IOException, JAXBException {
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
		clientActor.registerEvent(client);
	    }

	    waitingForFileToBeReadyToBeLaunched(client);
	}
    }

    /**
     * Take in charge of the file from it's detection until it is ready to be
     * treated.
     * 
     * @param client
     * @throws IOException
     */
    private void waitingForFileToBeReadyToBeLaunched(Client client) throws IOException {
	// looking at each detected files per client
	for (FileWatched file : client.getFilesWatched()) {
	    // present time
	    int actualTime = LocalDateTime.now().getHour() * 3600 + LocalDateTime.now().getMinute() * 60
		    + LocalDateTime.now().getSecond();

	    // registration of the file
	    if (!file.isRegistered()) {
		registerFile(client, file);
	    } else {
		LOG.debug("file {} already registered", file.getFilename());
	    }

	    // checking the waited delay from the arrived time of the file until
	    // now
	    if (file.getArrivingTime() + LogChainerConstante.DELAY_TRANSFER_FILE < actualTime) {
		LOG.debug("enough time waited");
		file.setReadyToBeTreated(true);
	    }
	}

	// registering all the treated files into a list
	ArrayList<String> allDoneFlux = new ArrayList<>();
	// iterating over all the flux
	for (Map.Entry<String, ArrayList<FileWatched>> flux : client.getFluxFileMap().entrySet()) {
	    // checking if the file can be treated
	    if (isFluxReadyToBeTreated(flux)) {
		// flux treatment
		fluxTreatment(client, allDoneFlux, flux);
		LOG.info("treatment of flux {} completed", flux.getKey());
	    }
	}
	// once all files in a flux have been treated, deleting the flux in the
	// map
	clientActor.deleteAllTreatedFluxFromMap(allDoneFlux, client);
    }

    /**
     * Check if the flux can be treated.
     * 
     * @param flux
     * @return
     */
    private boolean isFluxReadyToBeTreated(Map.Entry<String, ArrayList<FileWatched>> flux) {
	boolean fluxReadyToBeTreated = true;
	// checking if all files in a flux are ready to be treated
	for (FileWatched file : flux.getValue()) {
	    // check of the file
	    if (!file.isReadyToBeTreated())
		fluxReadyToBeTreated = false;
	}

	if (fluxReadyToBeTreated)
	    LOG.debug("flux ready to be treated");

	return fluxReadyToBeTreated;
    }

    /**
     * Trigger the flux treatment.
     * 
     * @param client
     * @param allDoneFlux
     * @param flux
     * @throws IOException
     */
    private void fluxTreatment(Client client, ArrayList<String> allDoneFlux,
	    Map.Entry<String, ArrayList<FileWatched>> flux) throws IOException {
	LOG.debug("flux {} starting to be treated", flux.getKey());
	sortFiles(component.getSeparator(client), component.getSorter(client), flux.getValue());
	LOG.debug("flux sorted");

	// cheking if all files' treatment has been completed
	boolean finished = true;
	// iterating on all the files of one flux
	for (FileWatched file : flux.getValue()) {
	    String filename = file.getFilename();
	    Path filePath = Paths.get(filename);
	    // checking if the file's treatment is complete
	    if (!treatmentAfterDetectionOfEvent(client, filePath, file))
		finished = false;
	}
	// registering the flux as completed (thus ready for deletion)
	if (finished) {
	    allDoneFlux.add(flux.getKey());
	    LOG.info("flux {} entirely treated", flux.getKey());
	}
    }

    /**
     * Register the file and relates it with it's client.
     * 
     * @param client
     * @param file
     */
    private void registerFile(Client client, FileWatched file) {
	LOG.debug("registering file {}", file.getFilename());
	Path filename = Paths.get(file.getFilename());
	// getting the name of the file's flux
	String fluxname = fluxActor.getFluxName(filename, component.getSeparator(client));

	// registering the flux if it doesn't already exist
	if (fluxActor.isNewFlux(fluxname, client)) {
	    fluxActor.addFlux(fluxname, client);
	    LOG.debug("new flux {} added to the map", fluxname);
	}

	// registering the file as a relation to it's flux
	fluxActor.addFileToFlux(fluxname, file, client);
	// indicating the file has been registered
	file.setRegistered(true);
	LOG.debug("file {} registered", file.getFilename());
    }

    /**
     * Sort the files of a specified flux using a specified sorting type.
     * 
     * @param separator
     * @param sorter
     * @param files
     */
    private void sortFiles(String separator, String sorter, ArrayList<FileWatched> files) {
	LOG.debug("sorting the file list");
	if (("alphabetical").equals(sorter)) {
	    LOG.debug("sorting by alphabetical order");
	} else {
	    LOG.debug("sorting by numerical order");
	}
	// sorting algorithm
	Collections.sort(files, new Comparator<FileWatched>() {
	    @Override
	    public int compare(FileWatched file1, FileWatched file2) {
		// getting both file's stamp which are used to sort them
		String sortingStamp1 = fluxActor.getSortingStamp(file1.getFilename(), separator);
		String sortingStamp2 = fluxActor.getSortingStamp(file2.getFilename(), separator);

		// case where the sorting type is alphabetical
		if (("alphabetical").equals(sorter)) {
		    return sortingStamp1.compareTo(sortingStamp2);
		} else {
		    // case where the sorting type is numerical (default one)
		    int stamp1 = Integer.parseInt(sortingStamp1);
		    int stamp2 = Integer.parseInt(sortingStamp2);
		    if (stamp1 < stamp2) {
			return -1;
		    } else if (stamp1 == stamp2) {
			return 0;
		    } else {
			return 1;
		    }
		}
	    }
	});
    }

    /**
     * Control in which way the file will be handled.
     * 
     * @param clientNb
     * @throws IOException
     * @throws LogChainerException
     */
    private boolean treatmentAfterDetectionOfEvent(Client client, Path filePath, FileWatched file) throws IOException {
	LOG.debug("start of the file treatment");
	// handling the overflow situation
	if (file.getKind() == OVERFLOW) {
	    LOG.debug("overflow detected");
	}

	// handling a file creation case
	if (file.getKind() == ENTRY_CREATE) {
	    // launching the file treatment
	    newFileTreatment(client, filePath);
	}

	// handling the file situation once it's treatment is done
	if (reset(client, file)) {
	    LOG.info("file {} treated", filePath.getFileName());
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Treatment of the file as an entry create.
     * 
     * @param clientNb
     * @param filePath
     * @throws IOException
     * @throws LogChainerException
     */
    private void newFileTreatment(Client client, Path filePath) throws IOException {
	LOG.debug("New file detected : {}",
		(new File(client.getConf().getInputDir() + "/" + filePath.toString())).getAbsolutePath());

	// accessing same flux file in the tmp directory
	Collection<File> previousFiles = getPreviousFiles(fluxActor.getFluxName(filePath, component.getSeparator(client)),
		client.getConf().getWorkingDir(), component.getSeparator(client));

	// moving the file to the tmp directory
	String pFileInTmp = mover.moveFileInputToTmp(filePath.toString(), client.getConf().getInputDir(),
		client.getConf().getWorkingDir());

	// chaining the log of the previous file to the current one (with infos:
	// previous file name and date of chaining)
	chainer.chainingLogFile(pFileInTmp, 0, messageToInsert(getPreviousFileHash(previousFiles), previousFiles)
		.getBytes(component.getEncodingType(client)));

	// releasing the file treated into the output directory to be taken in
	// charge by the user
	mover.moveFileTmpToOutput(filePath.toString(), client.getConf().getWorkingDir(),
		client.getConf().getOutputDir());

	if (!filePath.toString().isEmpty())
	    LOG.info("end of the treatment of the file {} put in the input directory", filePath.toString());
    }

    /**
     * Create the text message to insert in the new file.
     * 
     * @param filePath
     * @param hashCodeOfLog
     * @param oldFiles
     * @return the message
     */
    private String messageToInsert(byte[] hashCodeOfLog, Collection<File> oldFiles) {
	LOG.debug("computing the message to insert");
	// Chaining date
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	String date = "<Date of chaining: " + dateFormat.format(new Date()) + "> \n";

	// Name of the previous file
	String previousFile = "<Previous file: ";
	if (oldFiles.stream().findFirst().isPresent()) {
	    previousFile += oldFiles.stream().findFirst().get().getName() + "> \n";
	} else {
	    previousFile += "none> \n";
	}

	// HashCode of the previous file
	String previousFileHashCode = "<SHA-256: " + new String(hashCodeOfLog) + "> \n";

	LOG.debug("message ready to be inserted");
	return previousFile + date + previousFileHashCode;
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
	    // TODO
	    // ??????? error a remonter ?????
	    LOG.debug("could not delete the file from list");
	    return false;
	}
    }

    /**
     * Get the hash of the tmp directory's already existing file.
     * 
     * @param oldFiles
     * @return old file's hashCode
     * @throws IOException
     * @throws FileNotFoundException
     */
    private byte[] getPreviousFileHash(Collection<File> oldFiles) throws IOException {
	byte[] hashCodeOfLog;
	// hashing the provided file, with null hash if there's no file (=no
	// previous same flux file)
	if (oldFiles.stream().findFirst().isPresent()) {
	    File oldFile = oldFiles.stream().findFirst().get();
	    try (InputStream is = new FileInputStream((oldFile))) {
		LOG.debug("inputStream of the previous file opened");
		hashCodeOfLog = hasher.getLogHashCode(is);
	    }
	    LOG.debug("previous file name is : {}", oldFile.getName());
	    if (oldFile.delete())
		LOG.debug("previous file deleted");
	} else {
	    hashCodeOfLog = hasher.getNullHash();
	    LOG.debug("null hash used");
	}
	LOG.debug("previous file hashCode computed");
	return hashCodeOfLog;
    }

    /**
     * Get the collection of all already existing same flux files in tmp
     * directory. (Should be only one)
     * 
     * @param fluxName
     * @return collection of these files
     */
    @SuppressWarnings("unchecked")
    private Collection<File> getPreviousFiles(String fluxName, String workingDir, String separator) {
	// filtering the files to only keep the same as given flux one (should
	// be unique)
	return FileUtils.listFiles(new File(workingDir), new IOFileFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return accept(new File(name));
	    }

	    @Override
	    public boolean accept(File file) {
		if (fluxName.equals(fluxActor.getFluxName(file.toPath().getFileName(), separator))) {
		    LOG.debug("same flux name noticed for {}", file.getName());
		} else {
		    LOG.debug("no same flux name detected");
		}
		return (fluxName.equals(fluxActor.getFluxName(file.toPath().getFileName(), separator)) ? true : false);
	    }
	}, null);
    }
}
