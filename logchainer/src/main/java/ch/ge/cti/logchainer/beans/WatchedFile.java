package ch.ge.cti.logchainer.beans;

import java.nio.file.WatchEvent;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean grouping all watched file related attributes.
 */
public class WatchedFile {
    private static final int CONVERT_HOUR_TO_SECONDS = 3600;
    private static final int CONVERT_MINUTE_TO_SECONDS = 60;

    private final String filename;
    private final int arrivingTime;
    private boolean readyToBeProcessed = false;
    private WatchEvent.Kind<?> kind;
    private boolean registered;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(WatchedFile.class.getName());

    public WatchedFile(String filename) {
	LOG.debug("client infos instantiated");
	this.filename = filename;
	this.arrivingTime = LocalDateTime.now().getHour() * CONVERT_HOUR_TO_SECONDS
		+ LocalDateTime.now().getMinute() * CONVERT_MINUTE_TO_SECONDS + LocalDateTime.now().getSecond();
    }

    public String getFilename() {
	return filename;
    }

    public int getArrivingTime() {
	return arrivingTime;
    }

    public boolean isReadyToBeProcessed() {
	return readyToBeProcessed;
    }

    public void setReadyToBeProcessed(boolean readyToBeProcessed) {
	this.readyToBeProcessed = readyToBeProcessed;
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
