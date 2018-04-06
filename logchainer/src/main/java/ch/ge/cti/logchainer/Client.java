package ch.ge.cti.logchainer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ch.ge.cti.logchainer.generate.ClientConf;

/**
 * Bean grouping all client related attributs, such as the directories, the watcher
 * and it's watchKey.
 * 
 * @author FANICHETL
 *
 */
public class Client {
    private ClientConf conf;
    private WatchService watcher;
    private WatchKey key;
    private int arrivedTime;
    @Autowired
    private ArrayList<String> eventList;
    @Autowired
    private Map<String, Integer> fileArrivingTimeMap;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Client.class.getName());

    public Client(ClientConf conf) throws IOException {
	LOG.debug("creating object Client");
	this.conf = conf;
	this.watcher = FileSystems.getDefault().newWatchService();
    }

    public WatchKey getKey() {
	return key;
    }

    @SuppressWarnings("unchecked")
    public void setKey(WatchKey key) {
	LOG.debug("setting the key");
	this.key = key;
	
	WatchKey tempKey = key;
	for (WatchEvent<?> event : tempKey.pollEvents()) {
	    eventList.add(((WatchEvent<Path>) event.context()).toString());
	}
    }

    public WatchService getWatcher() {
	return watcher;
    }

    public ClientConf getConf() {
	return conf;
    }
    
    public int getArrivedTime() {
        return arrivedTime;
    }

    public void setArrivedTime(int arrivedTime) {
        this.arrivedTime = arrivedTime;
    }

    public ArrayList<String> getEventList() {
        return eventList;
    }

    public void addFileWithArrivingTime(String filename, Integer time) {
	fileArrivingTimeMap.put(filename, time);
    }

    public Map<String, Integer> getFileArrivingTimeMap() {
        return fileArrivingTimeMap;
    }
}
