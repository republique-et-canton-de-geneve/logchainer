package ch.ge.cti.logchainer.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileInputStream;
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

@Service
public class LogWatcherServiceImpl implements LogWatcherService {
    private Path input;
    private WatchService watcher;
    private WatchKey key;

    @Value("${inputDirectory}")
    private String inputDir;

    @Value("${tmpDirectory}")
    private String tmpDir;

    @Value("${outputDirectory}")
    private String outputDir;

    @Autowired
    private FolderService mover;
    private LogChainerService chainer;
    private HashService hasher;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogWatcherServiceImpl.class.getName());

    /**
     * Constructor.
     * 
     * @throws IOException
     */
    public LogWatcherServiceImpl() throws IOException {
	LOG.info("LogWatcherServiceImpl initialization started");

	this.input = Paths.get(inputDir);

	LOG.info("input file directory is : ", input.toString());

	this.watcher = FileSystems.getDefault().newWatchService();

	try {
	    key = input.register(watcher, ENTRY_CREATE);

	    LOG.info("key created as an ENTRY_CREATE");

	} catch (IOException e) {
	    LOG.error("couldn't complete the initialization : " + e.toString(), e);
	    throw e;
	}

	LOG.info("LogWatcherServiceImpl initialization completed");
    }

    @Override
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
		@SuppressWarnings("unchecked")
		WatchEvent<Path> ev = (WatchEvent<Path>) event;
		Path filename = ev.context();

		if (kind == ENTRY_CREATE) {
		    // We now refer to the code part
		    // treating of a new file appearing
		    // in the directoy.
		    LOG.info("New file detected : "
			    + (new File(input.toString() + "/" + filename.toString())).getAbsolutePath());

		    String fluxNameTmp = "";

		    for (int i = 0; i < filename.toString().toCharArray().length; ++i) {
			if (filename.toString().toCharArray()[i] != '_') {
			    fluxNameTmp += filename.toString().toCharArray()[i];
			} else {
			    i = filename.toString().toCharArray().length + 1;
			}
		    }
		    LOG.info("the flux of the file is : " + fluxNameTmp);
		    final String fluxName = fluxNameTmp;

		    @SuppressWarnings("unchecked")
		    Collection<File> oldTmpFile = FileUtils.listFiles(new File(tmpDir), new IOFileFilter() {
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

		    String pFileInTmp = mover.moveFileInputToTmp(filename.toString(), input.toString(), tmpDir);

		    // we instantiate a local array to keep and manipulate the
		    // hashCode
		    byte[] hashCodeOfLog;

		    if (!oldTmpFile.isEmpty()) {
			File oldFile = oldTmpFile.stream().findFirst().get();
			try (InputStream is = new FileInputStream((oldFile))) {
			    LOG.info("inputStream of the old file opened");
			    hashCodeOfLog = hasher.getLogHashCode(is);
			}
			LOG.info("old file name is : " + oldFile.getName());
			oldFile.delete();
			LOG.info("old file deleted");
		    } else {
			hashCodeOfLog = hasher.getNullHash();
			LOG.info("null hash used");
		    }

		    LOG.info("Hash of the logs received in hashCodeOfLog variable");

		    LOG.info("hash code is : <" + new String(hashCodeOfLog) + ">");

		    chainer.chainingLogFile(pFileInTmp, 0,
			    new String("<SHA-256: " + new String(hashCodeOfLog) + "> \n").getBytes());

		    mover.moveFileTmpToOutput(tmpDir, filename.toString(), outputDir);

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
}
