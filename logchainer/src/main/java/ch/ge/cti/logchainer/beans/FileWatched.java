package ch.ge.cti.logchainer.beans;

import java.nio.file.WatchEvent;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Bean grouping all watched file related attributes.
 * 
 * @author FANICHETL
 *
 */
@Component
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

    @Bean
    public String getFilename() {
	return filename;
    }

    @Bean
    public int getArrivingTime() {
	return arrivingTime;
    }

    @Bean
    public boolean isReadyToBeTreated() {
	return readyToBeTreated;
    }

    @Bean
    public void setReadyToBeTreated(boolean readyToBeTreated) {
	this.readyToBeTreated = readyToBeTreated;
    }

    @Bean
    public WatchEvent.Kind<?> getKind() {
	return kind;
    }

    @Bean
    public void setKind(WatchEvent.Kind<?> kind) {
	this.kind = kind;
    }

    @Bean
    public boolean isRegistered() {
        return registered;
    }

    @Bean
    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
