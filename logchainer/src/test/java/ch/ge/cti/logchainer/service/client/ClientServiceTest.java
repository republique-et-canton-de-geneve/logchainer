/*
 * <Log Chainer>
 *
 * Copyright (C) 2018 R�publique et Canton de Gen�ve
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

package ch.ge.cti.logchainer.service.client;

import static ch.ge.cti.logchainer.constant.LogChainerConstant.ENCODING_TYPE_DEFAULT;
import static ch.ge.cti.logchainer.constant.LogChainerConstant.SEPARATOR_DEFAULT;
import static ch.ge.cti.logchainer.constant.LogChainerConstant.SORT_DEFAULT;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.FilePattern;
import ch.ge.cti.logchainer.service.flux.FluxServiceImpl;
import ch.ge.cti.logchainer.service.helper.FileHelper;

public class ClientServiceTest {
    private final ClientServiceImpl clientService = new ClientServiceImpl();
    private Client client;
    private final int COUNTER_ITERATION_NB_BEFORE_FILE_CREATION = 50;
    private final String testResourcesDirPath = "src/test/resources/dirCreationDetectionTest";

    @BeforeClass
    public void setUp() throws IOException {
	ClientConf clientConf = new ClientConf();
	FilePattern filePattern = new FilePattern();

	filePattern.setSeparator(SEPARATOR_DEFAULT);
	filePattern.setSortingType(SORT_DEFAULT);

	clientConf.setFilePattern(filePattern);
	clientConf.setClientId("ClientTest");
	clientConf.setFileEncoding(ENCODING_TYPE_DEFAULT);
	clientConf.setInputDir(testResourcesDirPath);

	client = new Client(clientConf);

	// set the key of the client
	Path inputDirPath = Paths.get(client.getConf().getInputDir());
	WatchService watcher = client.getWatcher();

	client.setKey(inputDirPath.register(watcher, ENTRY_CREATE));
    }

    @Test(description = "testing the registration of an event")
    public void registering_an_event_should_comply_with_a_process() throws IOException {
	FileHelper fileHelper = mock(FileHelper.class);
	clientService.fileHelper = fileHelper;

	when(fileHelper.getSeparator(any(Client.class))).thenReturn(SEPARATOR_DEFAULT);

	// test the case where the filename isn't valid
	boolean loop = true;
	int counter = 0;
	String refFilename = "testMovingFile1.txt";

	while (loop) {
	    WatchKey watchKey = client.getWatcher().poll();

	    if (watchKey != null) {
		client.setKey(watchKey);
		assertEquals(clientService.registerEvent(client, false).get(0).getFilename(), refFilename,
			"corrupted files incorrectly processed");

		// reset the to be able to use it again
		if (!client.getKey().reset())
		    throw new IOException("Key could not be reseted");

		loop = false;
	    }
	    if (counter > COUNTER_ITERATION_NB_BEFORE_FILE_CREATION) {
		String noData = "";
		Files.write(Paths.get(testResourcesDirPath + "/testMovingFile1.txt"), noData.getBytes());
	    }
	    ++counter;
	}
	Files.delete(Paths.get(testResourcesDirPath + "/testMovingFile1.txt"));

	// test the case where there is the arrival of a new file
	boolean loop2 = true;
	int counter2 = 0;
	String filename = "fluxTest_stampTest.txt";

	while (loop2) {
	    WatchKey watchKey = client.getWatcher().poll();

	    if (watchKey != null) {
		client.setKey(watchKey);
		assertNull(clientService.registerEvent(client, false), "file not registered");
		boolean fileInFilesWatchedList = false;
		for (int i = 0; i < client.getWatchedFiles().size(); ++i) {
		    if (client.getWatchedFiles().get(i).getFilename().equals(filename)) {
			fileInFilesWatchedList = true;
			assertFalse(client.getWatchedFiles().get(i).isRegistered(),
				"file set as registered when it shouldn't be");
		    }
		}
		assertTrue(fileInFilesWatchedList, "file not registered in the client's file watched list");

		// reset the key to be able to use it again
		if (!client.getKey().reset())
		    throw new IOException("Key could not be reseted");

		loop2 = false;
	    }
	    if (counter2 > COUNTER_ITERATION_NB_BEFORE_FILE_CREATION) {
		String noData = "";
		Files.write(Paths.get(testResourcesDirPath + "/" + filename), noData.getBytes());
	    }
	    ++counter2;
	}
	Files.delete(Paths.get(testResourcesDirPath + "/fluxTest_stampTest.txt"));

	// test the case where the file is already registered
	boolean loop3 = true;
	int counter3 = 0;

	while (loop3) {
	    WatchKey watchKey = client.getWatcher().poll();

	    if (watchKey != null) {
		client.setKey(watchKey);
		assertNull(clientService.registerEvent(client, false), "file previously not registered and neither this time");
		boolean fileInFilesWatchedList = false;
		for (int i = 0; i < client.getWatchedFiles().size(); ++i) {
		    if (client.getWatchedFiles().get(i).getFilename().equals(filename)) {
			fileInFilesWatchedList = true;
		    }
		}
		assertTrue(fileInFilesWatchedList, "file not registered previously in the client's file watched list");

		// reset the to be able to use it again
		if (!client.getKey().reset())
		    throw new IOException("Key could not be reseted");

		loop3 = false;
	    }
	    if (counter3 > COUNTER_ITERATION_NB_BEFORE_FILE_CREATION) {
		String noData = "";
		Files.write(Paths.get(testResourcesDirPath + "/" + filename), noData.getBytes());
	    }
	    ++counter3;
	}
	Files.delete(Paths.get(testResourcesDirPath + "/fluxTest_stampTest.txt"));
    }

    @Test(description = "testing the deletion of the flux from the flux list of the client")
    public void deleting_the_flux_from_a_map_should_give_an_established_variable() {
	String fluxname = "fluxTest";
	client.getWatchedFilesByFlux().put(fluxname, (ArrayList<WatchedFile>) client.getWatchedFiles());
	List<String> doneFlux = new ArrayList<>();
	doneFlux.add(fluxname);

	FluxServiceImpl fluxService = mock(FluxServiceImpl.class);
	clientService.fluxService = fluxService;

	when(fluxService.removeFlux(anyString(), any(Client.class))).thenCallRealMethod();

	clientService.removeAllProcessedFluxesFromMap(doneFlux, client);
	assertTrue(client.getWatchedFilesByFlux().isEmpty(), "flux not deleted");
    }
}
