package ch.ge.cti.logchainer.monitoring;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.service.logwatcher.LogWatcherService;
import ch.ge.cti.logchainer.service.logwatcher.LogWatcherServiceImpl;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@Component
public class Metrics {
    private Counter counterTotalNbCorruptedFiles;
    private SimpleMeterRegistry registry = new SimpleMeterRegistry();

    public Metrics() {
	this.counterTotalNbCorruptedFiles = Counter.builder("corruptedFilesNb")
		.description("counts the number of files that have been transfered to the corruptedFiles directory")
		.tags("corrupted.files.detected.number", "/").register(registry);
    }

    public void registerTotalNbCorruptedFiles() {
	SimpleMeterRegistry registry = new SimpleMeterRegistry();

	LogWatcherServiceImpl.getClients().stream()
		.forEach(clientFromList -> getFilesAlreadyInDirectory(clientFromList.getConf().getCorruptedFilesDir())
			.stream().forEach(corrFile -> counterTotalNbCorruptedFiles.increment()));

	String nbCorruptedFiles = String.valueOf(counterTotalNbCorruptedFiles.count());

	Counter.builder("corruptedFilesNb")
		.description("counts the number of files that have been transfered to the corruptedFiles directory")
		.baseUnit("number of files").tags("corrupted.files.detected.number", nbCorruptedFiles)
		.register(registry);

	Controller.composite.add(registry);
    }

    public void registerAllCorruptedFiles() {
	Collection<File> corruptedFiles = new ArrayList<>();

	LogWatcherServiceImpl.getClients().stream().forEach(clientFromList -> corruptedFiles
		.addAll(getFilesAlreadyInDirectory(clientFromList.getConf().getCorruptedFilesDir())));

	corruptedFiles.stream()
		.forEach(corruptedFile -> Counter.builder(corruptedFile.getName())
			.description("this file has been put in the corrupted files directory").baseUnit("bytes")
			.tags("file.size", String.valueOf(corruptedFile.length())).register(Controller.composite));

    }

    public void registerAllCorruptedFiles(String clientId) {
	Collection<File> corruptedFilesForClient = new ArrayList<>();

	LogWatcherServiceImpl.getClients().stream().forEach(new Consumer<Client>() {
	    @Override
	    public void accept(Client client) {
		if (client.getConf().getClientId().equals(clientId)) {
		    corruptedFilesForClient.addAll(getFilesAlreadyInDirectory(client.getConf().getCorruptedFilesDir()));
		}
	    }
	});

	corruptedFilesForClient.stream()
		.forEach(corruptedFile -> Counter.builder(corruptedFile.getName())
			.description("this file has been put in the corrupted files directory").baseUnit("bytes")
			.tags("file.size", String.valueOf(corruptedFile.length())).register(Controller.composite));
    }

    @SuppressWarnings("unchecked")
    private Collection<File> getFilesAlreadyInDirectory(String workingDir) {
	return FileUtils.listFiles(new File(workingDir), new IOFileFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return accept(new File(name));
	    }

	    @Override
	    public boolean accept(File file) {
		return true;
	    }
	}, null);

    }

}
