package ch.ge.cti.logchainer.service.logwatcher;

import static ch.ge.cti.logchainer.constante.LogChainerConstante.DELAY_TRANSFER_FILE;
import static ch.ge.cti.logchainer.service.logwatcher.LogWatcherServiceImpl.CONVERT_HOUR_TO_SECONDS;
import static ch.ge.cti.logchainer.service.logwatcher.LogWatcherServiceImpl.CONVERT_MINUTE_TO_SECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;
import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.exception.CorruptedKeyException;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.FilePattern;
import ch.ge.cti.logchainer.generate.LogChainerConf;
import ch.ge.cti.logchainer.service.client.ClientServiceImpl;
import ch.ge.cti.logchainer.service.file.FileServiceImpl;
import ch.ge.cti.logchainer.service.folder.FolderServiceImpl;

public class LogWatcherServiceTest {
    private static final String testResourcesDirPath = "src/test/resources/dirCreationDetectionTest";
    private static final String testCorruptedFilesDir = "src/test/resources/testCorruptedFilesDir";
    private static final String testKeyBecomingInvalidDir = "src/test/resources/createDeleteDir";
    private static final LogWatcherServiceImpl watcher = new LogWatcherServiceImpl();
    private static final LogChainerConf clientConfList = new LogChainerConf();
    private static final Client client = new Client(new ClientConf());
    private static final String filename = "testDelayFile";
    private static final FileWatched testFile = new FileWatched(filename);

    @BeforeTest
    public void setUp() throws Exception {
	ClientConf clientConf = new ClientConf();

	clientConf.setFilePattern(new FilePattern());
	clientConf.setClientId("ClientTest");
	clientConf.setInputDir(testResourcesDirPath);
	clientConf.setCorruptedFilesDir(testCorruptedFilesDir);

	clientConfList.getListeClientConf().add(clientConf);

	ClientConf clientConf2 = new ClientConf();
	new File(testKeyBecomingInvalidDir).mkdir();

	clientConf2.setFilePattern(new FilePattern());
	clientConf2.setClientId("ClientTestToDisappear");
	clientConf2.setInputDir(testKeyBecomingInvalidDir);

	clientConfList.getListeClientConf().add(clientConf2);
    }

    @Test(description = "testing the initialization of the clients")
    public void testInitializeFileWatcherByClient() {
	watcher.initializeFileWatcherByClient(clientConfList);
	assertEquals(watcher.clients.size(), 2);
	assertEquals(watcher.clients.get(0).getConf(), clientConfList.getListeClientConf().get(0));
	assertNotNull(watcher.clients.get(0).getKey());
    }

    @Test(description = "testing the process of an event")
    public void testProcessEvents() throws IOException {
	String filename = "testCorruptedFile";
	String noData = "";

	ClientServiceImpl clientService = mock(ClientServiceImpl.class);
	watcher.clientService = clientService;
	FolderServiceImpl mover = mock(FolderServiceImpl.class);
	watcher.mover = mover;

	// test for a corrupted file
	when(clientService.registerEvent(any(Client.class))).thenReturn(new FileWatched(filename));
	when(mover.moveFileInDirWithNoSameNameFile(anyString(), anyString(), anyString())).thenCallRealMethod();

	Files.write(Paths.get(testResourcesDirPath + "/" + filename), noData.getBytes());
	watcher.processEvents();

	Collection<File> filesInCorruptedFilesDir = getPreviousFiles(testCorruptedFilesDir);
//	assertTrue(filesInCorruptedFilesDir.contains(new File(testCorruptedFilesDir + "/" + filename)));

	Files.delete(Paths.get(testCorruptedFilesDir + "/" + filename));

//	// test for a key becoming invalid
//	when(clientService.registerEvent(any(Client.class))).thenReturn(null);
//
//	Files.write(Paths.get(testKeyBecomingInvalidDir + "/" + filename), noData.getBytes());
//	Files.delete(Paths.get(testKeyBecomingInvalidDir + "/" + filename));
//	FileUtils.deleteDirectory(new File(testKeyBecomingInvalidDir));
//	try {
//	    watcher.processEvents();
//	} catch (BusinessException e) {
////	    assertEquals(e.getClass(), CorruptedKeyException.class);
//	}

//	// test of the delay waited before the process of a file
//	doNothing().when(clientService).deleteAllTreatedFluxFromMap(any(), any());
//
//	watcher.clients.clear();
//	watcher.clients.add(client);
//	client.getFilesWatched().add(testFile);
//	client.getFilesWatched().get(0).setRegistered(true);
//
//	boolean loop = true;
//	while (loop) {
//	    watcher.processEvents();
//	    if (testFile.isReadyToBeTreated())
//		loop = false;
//	}
//	int actualTime = LocalDateTime.now().getHour() * CONVERT_HOUR_TO_SECONDS
//		+ LocalDateTime.now().getMinute() * CONVERT_MINUTE_TO_SECONDS + LocalDateTime.now().getSecond();
////	assertTrue(actualTime - testFile.getArrivingTime() > DELAY_TRANSFER_FILE);
    }

    @Test(description = "testing the removal of a file after it's process")
    public void testTreatmentAfterDetectionOfEvent() {
	FileServiceImpl fileService = mock(FileServiceImpl.class);
	watcher.fileService = fileService;

	doNothing().when(fileService).newFileTreatment(any(Client.class), anyString());

	watcher.treatmentAfterDetectionOfEvent(client, filename, testFile);
	assertFalse(watcher.clients.get(0).getFilesWatched().contains(testFile));
    }

    @SuppressWarnings("unchecked")
    private Collection<File> getPreviousFiles(String workingDir) {
	// filtering the files to only keep the same as given flux one (should
	// be unique)
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
