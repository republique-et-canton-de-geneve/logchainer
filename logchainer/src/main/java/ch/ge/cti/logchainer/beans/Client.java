package ch.ge.cti.logchainer.beans;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    //private ArrayList<FileFromClient> filesWatched;
    private Map<String, ArrayList<FileWatched>> fluxFileMap;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Client.class.getName());

    public Client(ClientConf conf) throws IOException {
	LOG.debug("creating object Client");
	this.conf = conf;
	this.watcher = FileSystems.getDefault().newWatchService();
	this.filesWatched = new ArrayList<FileWatched>();
	this.fluxFileMap = new HashMap<String, ArrayList<FileWatched>>();
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

    public ArrayList<FileWatched> getFilesWatched() {
	return filesWatched;
    }
    
    public Map<String, ArrayList<FileWatched>> getFluxFileMap() {
        return fluxFileMap;
    }
    

}
