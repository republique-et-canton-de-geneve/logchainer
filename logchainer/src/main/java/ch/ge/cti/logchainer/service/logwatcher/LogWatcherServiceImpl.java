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
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.Collection;

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

import ch.ge.cti.logchainer.Client;
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
    public void processEvents() throws IOException {
	LOG.debug("LogWatcherServiceImpl initialization started");
	LogChainerConf clientConfList = new LogChainerConf();
	// Accessing the client list provided by the user
	try {
	    clientConfList = getDirPaths();
	    LOG.debug("client list accessed");
	} catch (JAXBException e) {
	    LOG.error("Exception while accessing configurations ", e);
	}

	// Registering all clients as Client objects in a list
	for (int clientNb = 0; clientNb < clientConfList.getClientConf().size(); clientNb++) {
	    clients.add(new Client(clientConfList.getClientConf().get(clientNb)));
	    LOG.debug("client {} added to the client list", clientConfList.getClientConf().get(clientNb).getClientId());

	    try {
		clients.get(clientNb).setKey(Paths.get(clients.get(clientNb).getConf().getInputDir())
			.register(clients.get(clientNb).getWatcher(), ENTRY_CREATE));

		LOG.debug("key created as an ENTRY_CREATE");

	    } catch (IOException e) {
		LOG.error("couldn't complete the initialization : {}", e.toString(), e);
		throw e;
	    }
	    LOG.debug("LogWatcherServiceImpl initialization completed");
	}

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
		    LOG.debug("event detected on client {}", clients.get(clientNb).getConf().getClientId());
		    clients.get(clientNb).setKey(watchKey);
		    treatmentAfterDetectionOfEvent(clientNb);
		}
	    }
	}
    }

    /**
     * Handle the file after it's detection.
     * 
     * @param clientNb
     * @throws IOException
     */
    private void treatmentAfterDetectionOfEvent(int clientNb) throws IOException {

	for (WatchEvent<?> event : clients.get(clientNb).getKey().pollEvents()) {
	    WatchEvent.Kind<?> kind = event.kind();

	    // handling the overflow situation
	    if (kind == OVERFLOW) {
		LOG.debug("overflow detected");
	    }

	    // To obtain the filename.
	    // the filename is the context of the event.
	    @SuppressWarnings("unchecked")
	    WatchEvent<Path> ev = (WatchEvent<Path>) event;
	    Path filePath = ev.context();

	    if (kind == ENTRY_CREATE) {
		// We now refer to the code part
		// treating of a new file appearing
		// in the directoy.
		newFileTreatment(clientNb, filePath);
	    }

	    // Reseting the key to be able to use it again
	    // If the key is not valid or the directory is inacessible
	    // ends the loop.
	    if (!keyReset(clientNb))
		break;
	}
    }

    /**
     * Treatment of the file as an entry create.
     * 
     * @param clientNb
     * @param filePath
     * @throws IOException
     */
    private void newFileTreatment(int clientNb, Path filePath) throws IOException {
	LOG.debug("New file detected : {}",
		(new File(clients.get(clientNb).getConf().getInputDir() + "/" + filePath.toString()))
			.getAbsolutePath());

	// accessing same flux file in the tmp directory
	Collection<File> oldFiles = getOldFiles(getFluxName(filePath), clients.get(clientNb).getConf().getWorkingDir());

	// moving the file to the tmp directory
	String pFileInTmp = mover.moveFileInputToTmp(filePath.toString(), clients.get(clientNb).getConf().getInputDir(),
		clients.get(clientNb).getConf().getWorkingDir());

	// we instantiate a local array to keep and manipulate the
	// hashCode of the previous same flux file
	byte[] hashCodeOfLog = getOldFileHash(oldFiles);

	// chaining the log of the previous file to the current one
	chainer.chainingLogFile(pFileInTmp, 0, ("<SHA-256: " + new String(hashCodeOfLog) + "> \n").getBytes());

	// releasing the file treated into the output directory to be taken in
	// charge by the user
	mover.moveFileTmpToOutput(filePath.toString(), clients.get(clientNb).getConf().getWorkingDir(),
		clients.get(clientNb).getConf().getOutputDir());

	LOG.debug("end of the treatment of the file put in the input directory");
    }

    /**
     * Reset of the watch key for further use and to see if it's still valid
     * 
     * @param clientNb
     * @return validity of the key
     */
    private boolean keyReset(int clientNb) {
	boolean valid = clients.get(clientNb).getKey().reset();
	if (!valid) {
	    LOG.error("directory of client {} is now inaccessible or it's key has become invalid",
		    clients.get(clientNb).getConf().getClientId());
	    return false;
	} else {
	    return true;
	}
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
    private Collection<File> getOldFiles(String fluxName, String workingDir) {
	// filtering the files to only keep the same as given flux one (should
	// be unique)
	return FileUtils.listFiles(new File(workingDir), new IOFileFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return accept(new File(name));
	    }

	    @Override
	    public boolean accept(File file) {
		LOG.debug("------------old file flux name {}", getFluxName(file.toPath().getFileName()));
		if (getFluxName(file.toPath().getFileName()) == fluxName) {
		    LOG.debug("same flux name noticed for {}", file.getName());
		} else {
		    LOG.debug("no same flux name detected");
		}
		return (getFluxName(file.toPath().getFileName()) == fluxName ? true : false);
	    }
	}, null);
    }

    /**
     * To get the flux name from a file
     * 
     * @param filename
     * @return fluxname
     */
    private String getFluxName(Path filename) {
	LOG.debug("getting flux name method entered");
	
	StringBuilder fluxNameTmp = new StringBuilder();
	boolean endFluxReached = false;

	// finding the flux name of the file, knowing each filename is of the
	// type :
	// fluxname_timestamp thus we stop at the '_' character
	for (int i = 0; i < filename.toString().toCharArray().length; ++i) {
	    if (filename.toString().toCharArray()[i] != '_' && !endFluxReached) {
		fluxNameTmp.append(filename.toString().toCharArray()[i]);
	    } else {
		endFluxReached = true;
	    }
	}
	LOG.debug("the flux of the file is : {}", fluxNameTmp.toString());

	return fluxNameTmp.toString();
    }

    /**
     * To access the directories' configurations.
     * 
     * @return
     * @throws JAXBException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private LogChainerConf getDirPaths() throws JAXBException, IOException {
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
