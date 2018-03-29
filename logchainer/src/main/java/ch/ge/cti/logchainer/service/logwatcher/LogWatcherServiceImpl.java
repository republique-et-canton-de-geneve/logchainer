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
    // default value is at the same level as the logchainer-base file
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
	LOG.info("LogWatcherServiceImpl initialization started");
	LogChainerConf clientConfList = new LogChainerConf();

	try {
	    clientConfList = getDirPaths();
	    LOG.info("client list accessed");
	} catch (JAXBException e) {
	    LOG.info("Exception while accessing configurations ", e);
	}

	for (int clientNb = 0; clientNb < clientConfList.getClientConf().size(); clientNb++) {
	    clients.add(new Client(clientConfList.getClientConf().get(clientNb)));
	    LOG.info("client {} added to the client list", clientConfList.getClientConf().get(clientNb).getClientId());

	    try {
		clients.get(clientNb).setKey(Paths.get(clients.get(clientNb).getConf().getInputDir())
			.register(clients.get(clientNb).getWatcher(), ENTRY_CREATE));

		LOG.info("key created as an ENTRY_CREATE");

	    } catch (IOException e) {
		LOG.error("couldn't complete the initialization : {}", e.toString(), e);
		throw e;
	    }
	    LOG.info("LogWatcherServiceImpl initialization completed");

	    LOG.info("start of the infinity loop");
	}

	for (;;) {
	    for (int clientNb = 0; clientNb < clientConfList.getClientConf().size(); clientNb++) {
		WatchKey watchKey = clients.get(clientNb).getWatcher().poll();

		if (watchKey != null) {
		    LOG.info("event detected on client {}", clients.get(clientNb).getConf().getClientId());
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
		LOG.info("overflow detected");
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
	LOG.info("New file detected : "
		+ (new File(clients.get(clientNb).getConf().getInputDir() + "/" + filePath.toString()))
			.getAbsolutePath());

	Collection<File> oldFiles = getOldFiles(getFluxName(filePath), clients.get(clientNb).getConf().getWorkingDir());

	String pFileInTmp = mover.moveFileInputToTmp(filePath.toString(), clients.get(clientNb).getConf().getInputDir(),
		clients.get(clientNb).getConf().getWorkingDir());

	// we instantiate a local array to keep and manipulate the
	// hashCode
	byte[] hashCodeOfLog;

	hashCodeOfLog = getOldFileHash(oldFiles);

	chainer.chainingLogFile(pFileInTmp, 0, ("<SHA-256: " + new String(hashCodeOfLog) + "> \n").getBytes());

	mover.moveFileTmpToOutput(filePath.toString(), clients.get(clientNb).getConf().getWorkingDir(),
		clients.get(clientNb).getConf().getOutputDir());

	LOG.info("end of the treatment of the file put in the input directory");
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

	    LOG.info("key isn't valid anymore or directory isn't accessible");
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
	if (oldFiles.stream().findFirst().isPresent()) {
	    File oldFile = oldFiles.stream().findFirst().get();
	    try (InputStream is = new FileInputStream((oldFile))) {
		LOG.info("inputStream of the old file opened");
		hashCodeOfLog = hasher.getLogHashCode(is);
	    }
	    LOG.info("old file name is : " + oldFile.getName());
	    if (oldFile.delete())
		LOG.info("old file deleted");
	} else {
	    hashCodeOfLog = hasher.getNullHash();
	    LOG.info("null hash used");
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
	return FileUtils.listFiles(new File(workingDir), new IOFileFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return accept(new File(name));
	    }

	    @Override
	    public boolean accept(File file) {
		if (file.getName().startsWith(fluxName)) {
		    LOG.info("same flux name noticed for " + file.getName());
		} else {
		    LOG.info("no same flux name detected");
		}
		return (file.getName().startsWith(fluxName) ? true : false);
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
	StringBuilder fluxNameTmp = new StringBuilder();
	boolean endFluxReached = false;

	for (int i = 0; i < filename.toString().toCharArray().length; ++i) {
	    if (filename.toString().toCharArray()[i] != '_' && !endFluxReached) {
		fluxNameTmp.append(filename.toString().toCharArray()[i]);
	    } else {
		endFluxReached = true;
	    }
	}
	LOG.info("the flux of the file is : {0}", fluxNameTmp.toString());

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
    private LogChainerConf getDirPaths() throws JAXBException, FileNotFoundException, IOException {
	JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
	Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

	LogChainerConf logchainerConf = new LogChainerConf();

	try (FileInputStream xmlFileStream = new FileInputStream(xmlDirectoriesConf)) {
	    logchainerConf = (LogChainerConf) unmarshaller.unmarshal(xmlFileStream);
	}

	return logchainerConf;
    }
}
