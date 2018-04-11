package ch.ge.cti.logchainer.beans;

import java.nio.file.WatchEvent;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWatched {
    private final String filename;
    private final int arrivingTime;
    private boolean readyToBeTreated = false;
    private WatchEvent.Kind<?> kind;
    private boolean registered;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FileWatched.class.getName());

    public FileWatched(String filename) {
	LOG.debug("client infos instantiated");
	this.filename = filename;
	this.arrivingTime = LocalDateTime.now().getHour() * 3600 + LocalDateTime.now().getMinute() * 60
		+ LocalDateTime.now().getSecond();
    }

    public String getFilename() {
	return filename;
    }

    public int getArrivingTime() {
	return arrivingTime;
    }

    public boolean isReadyToBeTreated() {
	return readyToBeTreated;
    }

    public void setReadyToBeTreated(boolean readyToBeTreated) {
	this.readyToBeTreated = readyToBeTreated;
    }

    public WatchEvent.Kind<?> getKind() {
	return kind;
    }

    public void setKind(WatchEvent.Kind<?> kind) {
	this.kind = kind;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
