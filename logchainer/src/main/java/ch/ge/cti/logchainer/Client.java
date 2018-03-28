package ch.ge.cti.logchainer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import ch.ge.cti.logchainer.generate.ClientConf;

public class Client {
    private ClientConf conf;
    private WatchService watcher;
    private WatchKey key;

    public Client(ClientConf conf) throws IOException {
	this.conf = conf;
	this.watcher = FileSystems.getDefault().newWatchService();
    }

    public WatchKey getKey() {
	return key;
    }

    public void setKey(WatchKey key) {
	this.key = key;
    }

    public WatchService getWatcher() {
	return watcher;
    }

    public ClientConf getConf() {
	return conf;
    }
}
