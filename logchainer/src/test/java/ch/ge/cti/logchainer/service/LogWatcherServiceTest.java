package ch.ge.cti.logchainer.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.configuration.TestConfiguration;

@ContextConfiguration(classes = TestConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class LogWatcherServiceTest extends AbstractTestNGSpringContextTests {
    private WatchService watchService;
    private WatchKey basePathWatchKey;
    private static String testResourcesDirPath = "src/test/resources/dirCreationDetectionTest";

    @BeforeTest
    public void setUp() throws Exception {
	watchService = FileSystems.getDefault().newWatchService();
	basePathWatchKey = Paths.get(testResourcesDirPath).register(watchService, ENTRY_CREATE);
    }

    @Test
    public void testEventForDirectory() throws Exception {
	String noData = "";
	Files.write(Paths.get(testResourcesDirPath + "/testFile1.txt"), noData.getBytes());
	Files.write(Paths.get(testResourcesDirPath + "/testFile2.txt"), noData.getBytes());
	Files.write(Paths.get(testResourcesDirPath + "/testFile3.txt"), noData.getBytes());

	WatchKey watchKey = watchService.poll(20, TimeUnit.SECONDS);
	assertNotNull(watchKey);
	assertEquals(watchKey, basePathWatchKey);
	List<WatchEvent<?>> eventList = watchKey.pollEvents();
	assertEquals(eventList.size(), 3);
	for (@SuppressWarnings("rawtypes")
	WatchEvent event : eventList) {
	    assertTrue(event.kind() == StandardWatchEventKinds.ENTRY_CREATE);
	    assertEquals(event.count(), 1);
	}

	Path eventPath = Paths.get(testResourcesDirPath + "/" + eventList.get(0).context().toString());
	assertTrue(Files.isSameFile(eventPath, Paths.get(testResourcesDirPath + "/testFile1.txt")));

	Path watchedPath = (Path) watchKey.watchable();
	assertTrue(Files.isSameFile(watchedPath, Paths.get(testResourcesDirPath)));

	Files.delete(Paths.get(testResourcesDirPath + "/testFile1.txt"));
	Files.delete(Paths.get(testResourcesDirPath + "/testFile2.txt"));
	Files.delete(Paths.get(testResourcesDirPath + "/testFile3.txt"));
    }

    @Test
    public void testEventForDirectoryWatchKey() throws Exception {
	String noData = "";
	Files.write(Paths.get(testResourcesDirPath + "/testFile4.txt"), noData.getBytes());

	List<WatchEvent<?>> eventList = basePathWatchKey.pollEvents();
	while (eventList.size() == 0) {
	    eventList = basePathWatchKey.pollEvents();
	    Thread.sleep(10000);
	}

	assertEquals(eventList.size(), 1);
	for (@SuppressWarnings("rawtypes")
	WatchEvent event : eventList) {
	    assertTrue(event.kind() == StandardWatchEventKinds.ENTRY_CREATE);
	}

	basePathWatchKey.reset();

	Files.write(Paths.get(testResourcesDirPath + "/testFile5.txt"), noData.getBytes());
	Files.write(Paths.get(testResourcesDirPath + "/testFile6.txt"), noData.getBytes());
	while (eventList.size() == 0) {
	    eventList = basePathWatchKey.pollEvents();
	    Thread.sleep(10000);
	}

	Path eventPath = Paths.get(testResourcesDirPath + "/" + eventList.get(0).context().toString());
	assertTrue(Files.isSameFile(eventPath, Paths.get(testResourcesDirPath + "/testFile4.txt")));

	Path watchedPath = (Path) basePathWatchKey.watchable();
	assertTrue(Files.isSameFile(watchedPath, Paths.get(testResourcesDirPath)));

	Files.delete(Paths.get(testResourcesDirPath + "/testFile4.txt"));
	Files.delete(Paths.get(testResourcesDirPath + "/testFile5.txt"));
	Files.delete(Paths.get(testResourcesDirPath + "/testFile6.txt"));
    }

}
