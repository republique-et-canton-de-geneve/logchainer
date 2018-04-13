package ch.ge.cti.logchainer.beans;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.generate.ClientConf;

/**
 * Bean grouping all client related attributs, such as the directories, the
 * watcher and it's watchKey.
 * 
 * @author FANICHETL
 *
 */
public class Client {
    private ClientConf conf;
    private WatchService watcher;
    private WatchKey key;
    // Variable des fichioers wahtched
    private ArrayList<FileWatched> filesWatched;
    // Map the flux to the files it contains
    private Map<String, ArrayList<FileWatched>> fluxFileMap;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Client.class.getName());

    public Client(ClientConf conf) {
	LOG.debug("creating object Client");
	this.conf = conf;
	try {
	    this.watcher = FileSystems.getDefault().newWatchService();
	} catch (IOException e) {
	    throw new BusinessException(this.conf.getClientId(), e);
	}
	this.filesWatched = new ArrayList<>();
	this.fluxFileMap = new HashMap<>();
    }

    public WatchKey getKey() {
	return key;
    }

    public void setKey(WatchKey key) {
	LOG.debug("setting the key");
	this.key = key;
    }

    public WatchService getWatcher() {
	return watcher;
    }

    public ClientConf getConf() {
	return conf;
    }

    public List<FileWatched> getFilesWatched() {
	return filesWatched;
    }

    public Map<String, ArrayList<FileWatched>> getFluxFileMap() {
	return fluxFileMap;
    }
}
