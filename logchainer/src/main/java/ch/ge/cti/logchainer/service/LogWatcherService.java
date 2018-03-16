package ch.ge.cti.logchainer.service;

import static ch.ge.cti.logchainer.constante.LogChainerConstante.TMP_DIRECTORY;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ge.cti.logchainer.configuration.AppConfiguration;

public class LogWatcherService {
    private Path input;
    private WatchService watcher;
    private WatchKey key;
    private String inputDir;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogWatcherService.class.getName());

    /**
     * Constructor.
     * 
     * @param pDir
     *            path to the directoy to watch.
     * @throws IOException
     */
    public LogWatcherService() throws IOException {
	LOG.info("initialization started");

	try {
	    inputDir = AppConfiguration.load().getProperty("inputDirectory");

	    LOG.info("input property correctly accessed");

	} catch (Exception e) {
	    LOG.error("input property not found", e);
	    throw e;
	}

	this.input = Paths.get(inputDir);

	LOG.info("file directory is : ", input.toString());

	this.watcher = FileSystems.getDefault().newWatchService();

	try {
	    key = input.register(watcher, ENTRY_CREATE);

	    LOG.info("key created as an ENTRY_CREATE");

	} catch (IOException e) {
	    LOG.error("couldn't complete the initialization : ", e.toString(), e);
	    throw e;
	}

	LOG.info("initialization completed");
    }

    /**
     * Infinity loop checking for updates in the directoy.
     * 
     * @throws Exception
     */
    public void processEvents() throws IOException {
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

		    continue;
		}

		// To obtain the filename if needed.
		// the filename is the context of the event.
		WatchEvent<Path> ev = (WatchEvent<Path>) event;
		Path filename = ev.context();

		if (kind == ENTRY_CREATE) {
		    // We now refer to the code part
		    // treating of a new file appearing
		    // in the directoy.
		    LOG.info("New file detected : "
			    + (new File(input.toString() + "/" + filename.toString())).getAbsolutePath());

		    String pFileInTmp;

		    pFileInTmp = FolderService.moveFileInputToTmp(filename.toString(), input.toString(),
			    AppConfiguration.getTmpProperty(TMP_DIRECTORY));

		    // we instantiate a local array to keep and manipulate the
		    // hashCode
		    byte[] hashCodeOfLog = HashService.getLogHashCode(pFileInTmp);

		    LOG.info("Hash of the logs received in hashCodeOfLog variable");

		    LOG.debug("hash code is : ", Arrays.toString(hashCodeOfLog), "  end");
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
}
