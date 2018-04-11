package ch.ge.cti.logchainer.clients;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
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
    private ArrayList<FileFromClient> filesFromClient;
    //private ArrayList<FileFromClient> filesWatched;
    private Map<String, ArrayList<FileFromClient>> fluxFileMap;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Client.class.getName());

    public Client(ClientConf conf) throws IOException {
	LOG.debug("creating object Client");
	this.conf = conf;
	this.watcher = FileSystems.getDefault().newWatchService();
	this.filesFromClient = new ArrayList<FileFromClient>();
	this.fluxFileMap = new HashMap<String, ArrayList<FileFromClient>>();
    }

//    public WatchKey getKey() {
//	return key;
//    }

    public void setKey(WatchKey key) {
	LOG.debug("setting the key");
	this.key = key;
    }

    @SuppressWarnings("unchecked")
    public void registerEvent() {
	for (WatchEvent<?> event : key.pollEvents()) {
	    FileFromClient clientInfos = new FileFromClient(((WatchEvent<Path>) event).context().toString());
	    boolean toRegister = true;

	    for (FileFromClient info : filesFromClient) {
		if (info.getFilename().equals(clientInfos.getFilename())) {
		    toRegister = false;
		    LOG.debug("file already registered");
		}
	    }

	    if (toRegister) {
		clientInfos.setKind(event.kind());
		clientInfos.setRegistered(false);
		filesFromClient.add(clientInfos);
		LOG.debug("file registered");
	    }
	}

	key.reset();
    }

    public WatchService getWatcher() {
	return watcher;
    }

    public ClientConf getConf() {
	return conf;
    }

    public ArrayList<FileFromClient> getFilesFromClient() {
	return filesFromClient;
    }
    
    public Map<String, ArrayList<FileFromClient>> getFluxFileMap() {
        return fluxFileMap;
    }
    
    public void addFlux(String fluxname) {
	fluxFileMap.put(fluxname, new ArrayList<FileFromClient>());
    }
    
    public boolean isNewFlux(String fluxname) {
	return !fluxFileMap.containsKey(fluxname);
    }
    
    public boolean removeFlux(String fluxname) {
	
	return fluxFileMap.remove(fluxname) != null; 
    }
    
    public void addFileToFlux(String fluxname, FileFromClient clientInfos) {
	fluxFileMap.get(fluxname).add(clientInfos);
    }
}
