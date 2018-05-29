/*
 * <Log Chainer>
 *
 * Copyright (C) 2018 République et Canton de Genève
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.ge.cti.logchainer.service.logwatcher;

import static ch.ge.cti.logchainer.constant.LogChainerConstant.DELAY_TRANSFER_FILE;
import static ch.ge.cti.logchainer.service.logwatcher.LogWatcherServiceImpl.CONVERT_HOUR_TO_SECONDS;
import static ch.ge.cti.logchainer.service.logwatcher.LogWatcherServiceImpl.CONVERT_MINUTE_TO_SECONDS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;
import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.exception.CorruptedKeyException;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.FilePattern;
import ch.ge.cti.logchainer.generate.LogChainerConf;
import ch.ge.cti.logchainer.service.client.ClientServiceImpl;
import ch.ge.cti.logchainer.service.file.FileServiceImpl;
import ch.ge.cti.logchainer.service.flux.FluxServiceImpl;
import ch.ge.cti.logchainer.service.folder.FolderServiceImpl;

public class LogWatcherServiceTest {
    private final String testResourcesDirPath = "src/test/resources/dirCreationDetectionTest";
    private final String testCorruptedFilesDir = "src/test/resources/testCorruptedFilesDir";
    private final String testKeyBecomingInvalidDir = "src/test/resources/createDeleteDir";
    private final LogWatcherServiceImpl watcher = new LogWatcherServiceImpl();
    private final LogChainerConf clientConfList = new LogChainerConf();
    private final Client client = new Client(new ClientConf());
    private final String filename = "testDelayFile";
    private final WatchedFile testFile = new WatchedFile(filename);
    private Client clientProbleme;

    @BeforeClass
    public void setUp() throws IOException {
	LogWatcherServiceImpl.clients.clear();

	ClientConf clientConf = new ClientConf();

	clientConf.setFilePattern(new FilePattern());
	clientConf.setClientId("ClientTest");
	clientConf.setInputDir(testResourcesDirPath);
	clientConf.setCorruptedFilesDir(testCorruptedFilesDir);

	clientConfList.getListeClientConf().add(clientConf);

	clientProbleme = new Client(clientConf);
	Path inputDirPath = Paths.get(clientProbleme.getConf().getInputDir());
	WatchService watcher = clientProbleme.getWatcher();

	clientProbleme.setKey(inputDirPath.register(watcher, ENTRY_CREATE));

	ClientConf clientConf2 = new ClientConf();
	new File(testKeyBecomingInvalidDir).mkdir();

	clientConf2.setFilePattern(new FilePattern());
	clientConf2.setClientId("ClientTestToDisappear");
	clientConf2.setInputDir(testKeyBecomingInvalidDir);

	clientConfList.getListeClientConf().add(clientConf2);

    }

    @Test(description = "testing the initialization of the clients")
    public void initializing_a_client_should_comply_with_a_process() {
	watcher.initializeFileWatcherByClient(clientConfList);
	assertEquals(LogWatcherServiceImpl.clients.size(), 2, "incorrect number of clients initialized");
	assertEquals(LogWatcherServiceImpl.clients.get(0).getConf(), clientConfList.getListeClientConf().get(0),
		"wrong clientConf initialized");
	assertNotNull(LogWatcherServiceImpl.clients.get(0).getKey(), "wrong key initialized");
    }

    @Test(description = "testing the process of an event")
    public void processing_events_should_comply_with_a_process() throws IOException {
	LogWatcherServiceImpl.clients.clear();
	LogWatcherServiceImpl.clients.add(clientProbleme);

	String filename = "testCorruptedFile";
	String noData = "";

	ClientServiceImpl clientService = mock(ClientServiceImpl.class);
	watcher.clientService = clientService;
	FolderServiceImpl mover = mock(FolderServiceImpl.class);
	watcher.mover = mover;
	FluxServiceImpl fluxService = mock(FluxServiceImpl.class);
	watcher.fluxService = fluxService;

	// test for a corrupted file
	List<WatchedFile> mockCorrFilesListToReturn = new ArrayList<>();
	mockCorrFilesListToReturn.add(new WatchedFile(filename));
	when(clientService.registerEvent(any(Client.class))).thenReturn(mockCorrFilesListToReturn);
	when(mover.moveFileInDirWithNoSameNameFile(anyString(), anyString(), anyString())).thenCallRealMethod();
	when(fluxService.isFluxReadyToBeProcessed(any())).thenReturn(true);
	doNothing().when(fluxService).corruptedFluxProcess(any(Client.class), any(), any());

	Files.write(Paths.get(testResourcesDirPath + "/" + filename), noData.getBytes());
	watcher.processEvents();

	Files.delete(Paths.get(testResourcesDirPath + "/" + filename));

	// test for a key becoming invalid
	when(clientService.registerEvent(any(Client.class))).thenReturn(null);

	Files.write(Paths.get(testKeyBecomingInvalidDir + "/" + filename), noData.getBytes());
	Files.delete(Paths.get(testKeyBecomingInvalidDir + "/" + filename));
	FileUtils.deleteDirectory(new File(testKeyBecomingInvalidDir));
	try {
	    watcher.processEvents();
	} catch (BusinessException e) {
	    assertEquals(e.getClass(), CorruptedKeyException.class, "CorruptedKeyException wasn't detected");
	}

	// test of the delay waited before the process of a file
	doNothing().when(clientService).removeAllProcessedFluxesFromMap(any(), any());

	LogWatcherServiceImpl.clients.clear();
	LogWatcherServiceImpl.clients.add(client);
	client.getWatchedFiles().add(testFile);
	client.getWatchedFiles().get(0).setRegistered(true);

	boolean loop = true;
	while (loop) {
	    watcher.processEvents();
	    if (testFile.isReadyToBeProcessed())
		loop = false;
	}
	int actualTime = LocalDateTime.now().getHour() * CONVERT_HOUR_TO_SECONDS
		+ LocalDateTime.now().getMinute() * CONVERT_MINUTE_TO_SECONDS + LocalDateTime.now().getSecond();
	assertTrue(actualTime - testFile.getArrivingTime() > DELAY_TRANSFER_FILE, "delay wasn't respected");
    }

    @Test(description = "testing the removal of a file after it's process")
    public void the_process_of_an_event_after_detection_should_comply_with_a_process() {
	LogWatcherServiceImpl.clients.clear();
	LogWatcherServiceImpl.clients.add(client);
	client.getWatchedFiles().clear();
	client.getWatchedFiles().add(testFile);

	FileServiceImpl fileService = mock(FileServiceImpl.class);
	watcher.fileService = fileService;

	doNothing().when(fileService).newFileProcess(any(Client.class), anyString());

	watcher.processAfterDetectionOfEvent(client, filename, testFile);
	assertFalse(LogWatcherServiceImpl.clients.get(0).getWatchedFiles().contains(testFile),
		"file not deleted from client's file list");
    }

    @AfterClass
    public void tearDown() {
	LogWatcherServiceImpl.clients.clear();
    }
}
