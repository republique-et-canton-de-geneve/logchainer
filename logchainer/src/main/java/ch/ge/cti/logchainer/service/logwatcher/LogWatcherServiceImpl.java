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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.clients.Client;
import ch.ge.cti.logchainer.clients.FileFromClient;
import ch.ge.cti.logchainer.exception.LogChainerException;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.LogChainerConf;
import ch.ge.cti.logchainer.generate.ObjectFactory;
import ch.ge.cti.logchainer.service.folder.FolderService;
import ch.ge.cti.logchainer.service.hash.HashService;
import ch.ge.cti.logchainer.service.logchainer.LogChainerService;

@Service
public class LogWatcherServiceImpl implements LogWatcherService {
    @Value("${xmlDirectoriesConf}")
    private String xmlDirectoriesConf;

    @Autowired
    private FolderService mover;
    @Autowired
    private LogChainerService chainer;
    @Autowired
    private HashService hasher;

    private ArrayList<Client> clients = new ArrayList<>();

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogWatcherServiceImpl.class.getName());

    @Override
    public void processEvents() throws IOException, JAXBException {
	LOG.debug("LogWatcherServiceImpl initialization started");
	LogChainerConf clientConfList = new LogChainerConf();
	// Accessing the client list provided by the user
	try {
	    clientConfList = loadConfiguration();
	    LOG.info("--------------------- client list accessed");
	} catch (JAXBException e) {
	    LOG.error("Exception while accessing configurations ", e);
	    throw e;
	}

	// Registering all clients as Client objects in a list
	initializeFileWatcherByClient(clientConfList);
	LOG.debug("LogWatcherServiceImpl initialization completed");

	// infinity loop to actualize endlessly the search for new files
	LOG.debug("start of the infinity loop");
	for (;;) {
	    // Iterating over all client for each iteration of the infinity loop
	    for (int clientNb = 0; clientNb < clientConfList.getClientConf().size(); clientNb++) {
		WatchKey watchKey = clients.get(clientNb).getWatcher().poll();

		// Launches the treatment only if a file was detected
		// No use of the take method because we don't want to wait until
		// an event is detected under one client
		// to move to the next one
		if (watchKey != null) {
		    LOG.info("--------------------- event detected on client {}",
			    clients.get(clientNb).getConf().getClientId());

		    clients.get(clientNb).setKey(watchKey);
		    clients.get(clientNb).registerEvent();
		}

		isFileTreatmentReadyToBeLaunched(clientNb);
	    }
	}
    }

    private void isFileTreatmentReadyToBeLaunched(int clientNb) throws IOException {
	// LOG.debug("finding if the treatment is to be launched");
	String separator;
	if (!clients.get(clientNb).getConf().getFilePattern().getSeparator().isEmpty()) {
	    separator = clients.get(clientNb).getConf().getFilePattern().getSeparator();
	} else {
	    separator = "_";
	}

	String sorter;
	if (!clients.get(clientNb).getConf().getFilePattern().getSortingType().isEmpty()) {
	    sorter = clients.get(clientNb).getConf().getFilePattern().getSortingType();
	} else {
	    sorter = "numerical";
	}

	for (FileFromClient file : clients.get(clientNb).getFilesFromClient()) {
	    int actualTime = LocalDateTime.now().getHour() * 3600 + LocalDateTime.now().getMinute() * 60
		    + LocalDateTime.now().getSecond();
	    // LOG.info("comparing arriving time with current time");

	    if (!file.isRegistered()) {
		Path filename = Paths.get(file.getFilename());
		String fluxname = getFluxName(filename, separator);

		if (clients.get(clientNb).isNewFlux(fluxname)) {
		    clients.get(clientNb).addFlux(fluxname);
		}

		clients.get(clientNb).addFileToFlux(fluxname, file);
		file.setRegistered(true);
	    }

	    if (file.getArrivingTime() + 10 < actualTime) {
		LOG.debug("enough time waited");
		file.setReadyToBeTreated(true);
	    }
	}

	for (String fluxname : clients.get(clientNb).getFluxFileMap().keySet()) {
	    boolean fluxReadyToBeTreated = true;
	    for (FileFromClient file : clients.get(clientNb).getFluxFileMap().get(fluxname)) {
		if (!file.isReadyToBeTreated())
		    fluxReadyToBeTreated = false;
	    }

	    if (fluxReadyToBeTreated) {
		LOG.debug("flux starting to be treated");
		sortFiles(clientNb, separator, sorter, fluxname);
		
		boolean finished = true;
		for (FileFromClient file : clients.get(clientNb).getFluxFileMap().get(fluxname)) {
		    String filename = file.getFilename();
		    Path filePath = Paths.get(filename);
		    int arrivingTime = file.getArrivingTime();
		    if (!treatmentAfterDetectionOfEvent(clientNb, filePath, arrivingTime))
			finished = false;
		}
		if (finished) {
		    if (clients.get(clientNb).removeFlux(fluxname))
			LOG.debug("flux treated and removed from list in client");
		}
	    }
	}
    }

    private void sortFiles(int clientNb, String separator, String sorter, String fluxname) {
	LOG.debug("sorting the file list");
	Collections.sort(clients.get(clientNb).getFluxFileMap().get(fluxname), new Comparator<FileFromClient>() {
	    @Override
	    public int compare(FileFromClient file1, FileFromClient file2) {
		String sortingStamp1 = getSortingStamp(file1.getFilename(), separator);
		String sortingStamp2 = getSortingStamp(file2.getFilename(), separator);

		if (("alphabetical").equals(sorter)) {
		    return sortingStamp1.compareTo(sortingStamp2);
		} else {
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
     * Create a list for all clients to be registered as Client objects in.
     * 
     * @param clientConfList
     * @throws IOException
     */
    private void initializeFileWatcherByClient(LogChainerConf clientConfList) throws IOException {
	// keeping track of the client number
	int clientNb = 0;
	for (ClientConf client : clientConfList.getClientConf()) {
	    clients.add(new Client(client));
	    LOG.info("client {} added to the client list", client.getClientId());

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

    /**
     * Handle the file after it's detection.
     * 
     * @param clientNb
     * @throws IOException
     * @throws LogChainerException
     */
    private boolean treatmentAfterDetectionOfEvent(int clientNb, Path filePath, int infoNb) throws IOException {
	// handling the overflow situation
	if (clients.get(clientNb).getFilesFromClient().get(infoNb).getKind() == OVERFLOW) {
	    LOG.debug("overflow detected");
	}

	if (clients.get(clientNb).getFilesFromClient().get(infoNb).getKind() == ENTRY_CREATE) {
	    // We now refer to the code part
	    // treating of a new file appearing
	    // in the directoy.
	    newFileTreatment(clientNb, filePath);
	}

	if (reset(clientNb, infoNb)) {
	    LOG.info("file {} treated", filePath.getFileName());
	    return true;
	} else {
	    return false;
	}


	//
	// // Reseting the key to be able to use it again
	// // If the key is not valid or the directory is inacessible
	// // ends the loop.
	// if (!keyReset(clientNb))
	// break;

    }

    /**
     * Treatment of the file as an entry create.
     * 
     * @param clientNb
     * @param filePath
     * @throws IOException
     * @throws LogChainerException
     */
    private void newFileTreatment(int clientNb, Path filePath) throws IOException {
	LOG.debug("New file detected : {}",
		(new File(clients.get(clientNb).getConf().getInputDir() + "/" + filePath.toString()))
		.getAbsolutePath());

	// define the separator for the file name components, is '_' by
	// default
	String separator;
	if (!clients.get(clientNb).getConf().getFilePattern().getSeparator().isEmpty()) {
	    separator = clients.get(clientNb).getConf().getFilePattern().getSeparator();
	} else {
	    separator = "_";
	}
	// accessing same flux file in the tmp directory
	Collection<File> oldFiles = getOldFiles(getFluxName(filePath, separator),
		clients.get(clientNb).getConf().getWorkingDir(), separator);

	// moving the file to the tmp directory
	String pFileInTmp = mover.moveFileInputToTmp(filePath.toString(), clients.get(clientNb).getConf().getInputDir(),
		clients.get(clientNb).getConf().getWorkingDir());

	// we instantiate a local array to keep and manipulate the
	// hashCode of the previous same flux file
	byte[] hashCodeOfLog = getOldFileHash(oldFiles);

	// define the encoding way for encryption into the file, is UTF-8 by
	// default
	String encodingType;
	if (!clients.get(clientNb).getConf().getFileEncoding().isEmpty()) {
	    encodingType = clients.get(clientNb).getConf().getFileEncoding();
	} else {
	    encodingType = "UTF-8";
	}
	// chaining the log of the previous file to the current one (with infos:
	// previous file name and date of chaining)
	chainer.chainingLogFile(pFileInTmp, 0,
		messageToInsert(filePath, hashCodeOfLog, oldFiles).getBytes(encodingType));

	// releasing the file treated into the output directory to be taken in
	// charge by the user
	mover.moveFileTmpToOutput(filePath.toString(), clients.get(clientNb).getConf().getWorkingDir(),
		clients.get(clientNb).getConf().getOutputDir());

	LOG.info("end of the treatment of the file put in the input directory");
    }

    /**
     * Create the text message to insert in the new file.
     * 
     * @param filePath
     * @param hashCodeOfLog
     * @param oldFiles
     * @return the message
     */
    private String messageToInsert(Path filePath, byte[] hashCodeOfLog, Collection<File> oldFiles) {
	// Chaining date
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	String date = "<Date of chaining: " + dateFormat.format(new Date()) + "> \n";

	// Name of the previous file
	String previousFile = "< Previous file: ";
	if (oldFiles.stream().findFirst().isPresent()) {
	    previousFile += oldFiles.stream().findFirst().get().getName() + "> \n";
	} else {
	    previousFile += "none> \n";
	}

	// HashCode of the previous file
	String previousFileHashCode = "<SHA-256: " + new String(hashCodeOfLog) + "> \n";

	return previousFile + date + previousFileHashCode;
    }

    /**
     * Reset of the watch key for further use and to see if it's still valid
     * 
     * @param clientNb
     * @return validity of the key
     */
    private boolean reset(int clientNb, int infoNb) {
	if (clients.get(clientNb).getFilesFromClient().remove(infoNb) != null) {
	    LOG.debug("file references successfully removed from map");
	    return true;
	} else {
	    // TODO
	    // ??????? error a remonter ?????
	    LOG.debug("could not delete the file from map");
	    ;
	    return false;
	}
	// boolean valid = clients.get(clientNb).getKey().reset();
	// if (!valid) {
	// LOG.error("directory of client {} is now inaccessible or it's key has
	// become invalid",
	// clients.get(clientNb).getConf().getClientId());
	// return false;
	// } else {
	// return true;
	// }
    }

    /**
     * To get the hash of the tmp directory's already existing file
     * 
     * @param oldFiles
     * @return old file's hashCode
     * @throws IOException
     * @throws FileNotFoundException
     */
    private byte[] getOldFileHash(Collection<File> oldFiles) throws IOException {
	byte[] hashCodeOfLog;
	// hashing the provided file, with null hash if there's no file (=no
	// previous same flux file)
	if (oldFiles.stream().findFirst().isPresent()) {
	    File oldFile = oldFiles.stream().findFirst().get();
	    try (InputStream is = new FileInputStream((oldFile))) {
		LOG.debug("inputStream of the old file opened");
		hashCodeOfLog = hasher.getLogHashCode(is);
	    }
	    LOG.debug("old file name is : {}", oldFile.getName());
	    if (oldFile.delete())
		LOG.debug("old file deleted");
	} else {
	    hashCodeOfLog = hasher.getNullHash();
	    LOG.debug("null hash used");
	}

	return hashCodeOfLog;
    }

    /**
     * To get the collection of all already existing same flux files
     * 
     * @param fluxName
     * @return collection of these files
     */
    @SuppressWarnings("unchecked")
    private Collection<File> getOldFiles(String fluxName, String workingDir, String separator) {
	// filtering the files to only keep the same as given flux one (should
	// be unique)
	return FileUtils.listFiles(new File(workingDir), new IOFileFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return accept(new File(name));
	    }

	    @Override
	    public boolean accept(File file) {
		if (fluxName.equals(getFluxName(file.toPath().getFileName(), separator))) {
		    LOG.debug("same flux name noticed for {}", file.getName());
		} else {
		    LOG.debug("no same flux name detected");
		}
		return (fluxName.equals(getFluxName(file.toPath().getFileName(), separator)) ? true : false);
	    }
	}, null);
    }

    /**
     * To get the flux name from a file
     * 
     * @param filename
     * @return fluxname
     * @throws LogChainerException
     */
    private String getFluxName(Path filename, String separator) {
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

    private String getSortingStamp(String filename, String separator) {
	String[] nameComponents = filename.split(separator);
	String[] nameStampComponents = nameComponents[nameComponents.length-1].split("\\.");

	return nameStampComponents[0];
    }

    /**
     * To access the directories' configurations.
     * 
     * @return
     * @throws JAXBException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private LogChainerConf loadConfiguration() throws JAXBException, IOException {
	LOG.debug("starting to read conf file");

	JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
	Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

	LogChainerConf logchainerConf = new LogChainerConf();

	// accessing the xml conf file
	try (FileInputStream xmlFileStream = new FileInputStream(xmlDirectoriesConf)) {
	    logchainerConf = (LogChainerConf) unmarshaller.unmarshal(xmlFileStream);
	}
	LOG.debug("conf file correctly accessed");

	return logchainerConf;
    }
}
