package ch.ge.cti.logchainer.monitoring;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.FilePattern;
import ch.ge.cti.logchainer.generate.LogChainerConf;
import ch.ge.cti.logchainer.service.logwatcher.LogWatcherServiceImpl;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class metricsAndControllerTest {
    private final Controller controller = new Controller();
    private final LogChainerConf clientConfList = new LogChainerConf();
    private final String testCorruptedFilesDir = "src/test/resources/testCorruptedFilesDir";
    private final String dirCreationDetectionTest = "src/test/resources/dirCreationDetectionTest";
    private final String clientNameA = "ClientTestA";
    private final String clientNameB = "ClientTestB";
    private final String filename = "corruptedFile1";
    private final String filename2 = "corruptedFile2";
    private final LogWatcherServiceImpl watcher = new LogWatcherServiceImpl();
    private Path filepath1;
    private Path filepath2;

    @BeforeClass
    public void setUp() throws IOException {
	ClientConf clientConf = new ClientConf();

	clientConf.setFilePattern(new FilePattern());
	clientConf.setClientId(clientNameA);
	clientConf.setInputDir("");
	clientConf.setCorruptedFilesDir(testCorruptedFilesDir);

	clientConfList.getListeClientConf().add(clientConf);

	ClientConf clientConf2 = new ClientConf();

	clientConf2.setFilePattern(new FilePattern());
	clientConf2.setClientId(clientNameB);
	clientConf2.setInputDir("");
	clientConf2.setCorruptedFilesDir(dirCreationDetectionTest);

	clientConfList.getListeClientConf().add(clientConf2);

	watcher.initializeFileWatcherByClient(clientConfList);

	String noData = "";
	filepath1 = Files.write(Paths.get(testCorruptedFilesDir + "/" + filename), noData.getBytes());
	filepath2 = Files.write(Paths.get(testCorruptedFilesDir + "/" + filename2), noData.getBytes());
    }

    @Test(description = "test of the reception of global infos about the corrupted files")
    public void global_infos_should_show_established_infos() {
	Set<String> refNames = new HashSet<>();
	Set<String> refTagsKey = new HashSet<>();
	Set<String> refTagsValue = new HashSet<>();
	Set<String> refDescription = new HashSet<>();
	Set<String> refBaseUnit = new HashSet<>();

	refNames.add("corruptedFilesNb");
	refTagsKey.add("corrupted.files.detected.number");
	refTagsValue.add(String.valueOf(4.0));
	refDescription.add("counts the number of files that have been transfered to the corruptedFiles directory");
	refBaseUnit.add("number of files");

	refNames.add(clientNameA);
	refTagsKey.add(filename);
	refTagsValue.add(String.valueOf(new File(testCorruptedFilesDir + "/" + filename).length()));
	refTagsKey.add(filename2);
	refTagsValue.add(String.valueOf(new File(testCorruptedFilesDir + "/" + filename2).length()));
	refDescription.add(String.valueOf(3.0) + " files were put in the corrupted files directory for this client");
	refBaseUnit.add("bytes (file size)");

	refNames.add(clientNameB);
	refDescription.add(String.valueOf(1.0) + " files were put in the corrupted files directory for this client");
	refBaseUnit.add("bytes (file size)");

	CompositeMeterRegistry obtainedRegistery = controller.globalInfos();
	SimpleMeterRegistry registeriesFromComposite = (SimpleMeterRegistry) obtainedRegistery.getRegistries()
		.toArray()[0];
	assertEquals(registeriesFromComposite.getMeters().size(), 3, "incorrect number of meters registered");
	registeriesFromComposite.getMeters().stream().forEach(new Consumer<Meter>() {
	    @Override
	    public void accept(Meter meter) {
		assertTrue(refNames.contains(meter.getId().getName()), "incorrect name");
		assertTrue(refBaseUnit.contains(meter.getId().getBaseUnit()), "incorrect base unit");
		meter.getId().getTags().stream().forEach(new Consumer<Tag>() {
		    @Override
		    public void accept(Tag tag) {
			if (!tag.getKey().equals("readme.txt")) {
			    assertTrue(refTagsKey.contains(tag.getKey()), "incorrect tag name (key)");
			    assertTrue(refTagsValue.contains(tag.getValue()), "incorrect tag value");
			}
		    }
		});
		assertTrue(refDescription.contains(meter.getId().getDescription()), "incorrect description");
	    }
	});
    }

    @Test(description = "test of the informations given about a client")
    public void infos_about_a_client_should_show_established_infos() {
	Set<String> refNames = new HashSet<>();
	Set<String> refTagsKey = new HashSet<>();
	Set<String> refTagsValue = new HashSet<>();
	Set<String> refDescription = new HashSet<>();
	Set<String> refBaseUnit = new HashSet<>();

	refNames.add(clientNameA);
	refNames.add(filename);
	refNames.add(filename2);
	refNames.add("readme.txt");

	refTagsKey.add("corrupted.files.number");
	refTagsKey.add("file.size");

	refTagsValue.add(String.valueOf(filepath1.toFile().length()));
	refTagsValue.add(String.valueOf(filepath2.toFile().length()));
	refTagsValue.add(String.valueOf(new File("src/test/resources/testCorruptedFilesDir/readme.txt").length()));
	refTagsValue.add(String.valueOf(3));

	refDescription.add("this file has been put in the corrupted files directory");
	refDescription.add("number of corrupted files for this client");

	refBaseUnit.add("bytes");
	refBaseUnit.add("number of files");

	CompositeMeterRegistry obtainedRegistery = controller.clientInfos(clientNameA);
	assertEquals(obtainedRegistery.getMeters().size(), 4, "incorrect number of meters registered");
	obtainedRegistery.getMeters().stream().forEach(new Consumer<Meter>() {
	    @Override
	    public void accept(Meter meter) {
		assertTrue(refNames.contains(meter.getId().getName()), "incorrect name");
		assertTrue(refBaseUnit.contains(meter.getId().getBaseUnit()), "incorrect base unit");
		meter.getId().getTags().stream().forEach(new Consumer<Tag>() {
		    @Override
		    public void accept(Tag tag) {
			if (!tag.getKey().equals("readme.txt")) {
			    assertTrue(refTagsKey.contains(tag.getKey()), "incorrect tag name (key)");
			    assertTrue(refTagsValue.contains(tag.getValue()), "incorrect tag value");
			}
		    }
		});
		assertTrue(refDescription.contains(meter.getId().getDescription()), "incorrect description");
	    }
	});

    }

    @AfterTest
    public void tearDown() throws IOException {
	Files.delete(Paths.get(testCorruptedFilesDir + "/" + filename));
	Files.delete(Paths.get(testCorruptedFilesDir + "/" + filename2));
    }
}
