package ch.ge.cti.logchainer.service.logwatcher;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.service.folder.FolderService;
import ch.ge.cti.logchainer.service.hash.HashService;
import ch.ge.cti.logchainer.service.logchainer.LogChainerService;

@Service
public class LogWatcherServiceImpl implements LogWatcherService {
    @Value("${inputDirectory:toto}")
    private String inputDir;

    @Value("${tmpDirectory}")
    private String tmpDir;

    @Value("${outputDirectory}")
    private String outputDir;

    @Autowired
    private FolderService mover;
    @Autowired
    private LogChainerService chainer;
    @Autowired
    private HashService hasher;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogWatcherServiceImpl.class.getName());

    @Override
    public void processEvents() throws IOException {
	LOG.info("LogWatcherServiceImpl initialization started");

	Path input = Paths.get(inputDir);
	LOG.info("input file directory is : ", inputDir);

	WatchService watcher = FileSystems.getDefault().newWatchService();

	WatchKey key;

	try {
	    key = input.register(watcher, ENTRY_CREATE);

	    LOG.info("key created as an ENTRY_CREATE");

	} catch (IOException e) {
	    LOG.error("couldn't complete the initialization : " + e.toString(), e);
	    throw e;
	}
	LOG.info("LogWatcherServiceImpl initialization completed");

	LOG.info("start of the infinity loop");

	for (;;) {

	    // wait for key to be signaled
	    try {
		key = watcher.take();
	    } catch (InterruptedException x) {
		LOG.error("interruption in the key search ", x);

		return;
	    }

	    for (WatchEvent<?> event : key.pollEvents()) {
		WatchEvent.Kind<?> kind = event.kind();

		// handling the overflow situation
		if (kind == OVERFLOW) {
		    LOG.info("overflow detected");
		}

		// To obtain the filename if needed.
		// the filename is the context of the event.
		@SuppressWarnings("unchecked")
		WatchEvent<Path> ev = (WatchEvent<Path>) event;
		Path filename = ev.context();

		if (kind == ENTRY_CREATE) {
		    // We now refer to the code part
		    // treating of a new file appearing
		    // in the directoy.
		    LOG.info("New file detected : "
			    + (new File(input.toString() + "/" + filename.toString())).getAbsolutePath());

		    Collection<File> oldFiles = getOldFiles(getFluxName(filename));

		    String pFileInTmp = mover.moveFileInputToTmp(filename.toString(), input.toString(), tmpDir);

		    // we instantiate a local array to keep and manipulate the
		    // hashCode
		    byte[] hashCodeOfLog;

		    hashCodeOfLog = getOldFileHash(oldFiles);

		    chainer.chainingLogFile(pFileInTmp, 0,
			    ("<SHA-256: " + new String(hashCodeOfLog) + "> \n").getBytes());

		    mover.moveFileTmpToOutput(filename.toString(), tmpDir, outputDir);

		    LOG.info("end of the treatment of the file put in the input directory");
		}

		// Reseting the key to be able to use it again
		// If the key is not valid or the directory is inacessible
		// ends the loop.
		boolean valid = key.reset();
		if (!valid) {

		    LOG.info("key isn't valid anymore or directory isn't accessible");
		    break;
		}
	    }
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
    private Collection<File> getOldFiles(final String fluxName) {
	return FileUtils.listFiles(new File(tmpDir), new IOFileFilter() {
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
	LOG.info("the flux of the file is : {}", fluxNameTmp.toString());

	return fluxNameTmp.toString();
    }
}
