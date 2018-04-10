package ch.ge.cti.logchainer.clients;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
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
    private Map<Integer, ClientInstanceInfos> timeInfosMap;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Client.class.getName());

    public Client(ClientConf conf) throws IOException {
	LOG.debug("creating object Client");
	this.conf = conf;
	this.watcher = FileSystems.getDefault().newWatchService();
	this.timeInfosMap = new HashMap<Integer, ClientInstanceInfos>();
    }

    public WatchKey getKey() {
	return key;
    }

    public void setKey(WatchKey key) {
	LOG.debug("setting the key");
	this.key = key;
    }

    @SuppressWarnings("unchecked")
    public void registerEvent() {
	for (WatchEvent<?> event : key.pollEvents()) {
	    ClientInstanceInfos clientInfos = new ClientInstanceInfos(((WatchEvent<Path>) event).context().toString());
	    boolean toRegister = true;

	    for (ClientInstanceInfos info : timeInfosMap.values()) {
		if (info.getFilename().equals(clientInfos.getFilename())) {
		    toRegister = false;
		    LOG.debug("file already registered");
		}
	    }

	    if (toRegister) {
		clientInfos.setKind(event.kind());
		timeInfosMap.put(clientInfos.getArrivingTime(), clientInfos);
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

    public Map<Integer, ClientInstanceInfos> getTimeInfosMap() {
	return timeInfosMap;
    }
}
