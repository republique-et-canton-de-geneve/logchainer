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
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import ch.ge.cti.logchainer.generate.ClientConf;

/**
 * Bean grouping all client related attributs, such as the directories, the
 * watcher and it's watchKey.
 * 
 * @author FANICHETL
 *
 */
@Component
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

    public Client(ClientConf conf) throws IOException {
	LOG.debug("creating object Client");
	this.conf = conf;
	this.watcher = FileSystems.getDefault().newWatchService();
	this.filesWatched = new ArrayList<>();
	this.fluxFileMap = new HashMap<>();
    }

    @Bean
    public WatchKey getKey() {
	return key;
    }

    @Bean
    public void setKey(WatchKey key) {
	LOG.debug("setting the key");
	this.key = key;
    }

    @Bean
    public WatchService getWatcher() {
	return watcher;
    }

    @Bean
    public ClientConf getConf() {
	return conf;
    }

    @Bean
    public List<FileWatched> getFilesWatched() {
	return filesWatched;
    }
    
    @Bean
    public Map<String, ArrayList<FileWatched>> getFluxFileMap() {
        return fluxFileMap;
    }
}
