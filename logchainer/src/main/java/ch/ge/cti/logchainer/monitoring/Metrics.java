//package ch.ge.cti.logchainer.monitoring;
//
//import java.io.File;
//import java.util.Collection;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.filefilter.IOFileFilter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import ch.ge.cti.logchainer.service.logwatcher.LogWatcherService;
//import io.micrometer.core.instrument.Counter;
//import io.micrometer.core.instrument.MeterRegistry;
//import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
//import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
//
//@Component
//public class Metrics {
//    private Counter counter;
//    private Collection<File> corruptedFiles;
//    private Collection<String> corruptedFilesName;
//    SimpleMeterRegistry registry = new SimpleMeterRegistry();
//
//    @Autowired
//    private LogWatcherService watcher;
//
//    public Metrics() {
//	CompositeMeterRegistry composite = new CompositeMeterRegistry(); 
//	composite.add(registry);
//	this.counter = Counter.builder("corruptedFilesNb")
//		.description("counts the number of files that have been transfered to the corruptedFiles directory")
//		.tags("corrupted.files.detected.number", "arf").register(composite);
//    }
//
//    public void saveNbCorruptedFilesTotal() {
//	watcher.getClients().stream()
//	.forEach(clientFromList -> getFilesAlreadyInDirectory(clientFromList.getConf().getCorruptedFilesDir())
//		.stream().forEach(corrFile -> counter.increment()));
//    }
//
//    public void saveAllCorruptedFiles() {
//	watcher.getClients().stream().forEach(clientFromList -> corruptedFiles
//		.addAll(getFilesAlreadyInDirectory(clientFromList.getConf().getCorruptedFilesDir())));
//	corruptedFiles.stream().forEach(fileFromList -> corruptedFilesName.add(fileFromList.getName()));
//    }
//
////    public MeterRegistry getRegistry() {
////	return registry;
////    }
//
//    @SuppressWarnings("unchecked")
//    private Collection<File> getFilesAlreadyInDirectory(String workingDir) {
//	// filtering the files to only keep the same as given flux one (should
//	// be unique)
//	return FileUtils.listFiles(new File(workingDir), new IOFileFilter() {
//	    @Override
//	    public boolean accept(File dir, String name) {
//		return accept(new File(name));
//	    }
//
//	    @Override
//	    public boolean accept(File file) {
//		return true;
//	    }
//	}, null);
//
//    }
//
//}
