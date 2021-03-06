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

package ch.ge.cti.logchainer.service.file;

import static ch.ge.cti.logchainer.constant.LogChainerConstant.ENCODING_TYPE_DEFAULT;
import static ch.ge.cti.logchainer.constant.LogChainerConstant.SEPARATOR_DEFAULT;
import static ch.ge.cti.logchainer.constant.LogChainerConstant.SORT_DEFAULT;
import static ch.ge.cti.logchainer.constant.LogChainerConstant.STAMP_POSITION_DEFAULT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;
import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.FilePattern;
import ch.ge.cti.logchainer.service.flux.FluxServiceImpl;
import ch.ge.cti.logchainer.service.folder.FolderServiceImpl;
import ch.ge.cti.logchainer.service.hash.HashServiceImpl;
import ch.ge.cti.logchainer.service.helper.FileHelper;
import ch.ge.cti.logchainer.service.logchainer.LogChainerServiceImpl;

public class FileServiceTest {
    private Client client;
    private final FileServiceImpl fileService = new FileServiceImpl();

    @BeforeClass
    public void setUp() {
	ClientConf clientConf = new ClientConf();
	FilePattern filePattern = new FilePattern();

	filePattern.setSeparator(SEPARATOR_DEFAULT);
	filePattern.setSortingType(SORT_DEFAULT);

	clientConf.setFilePattern(filePattern);
	clientConf.setClientId("ClientTest");
	clientConf.setFileEncoding(ENCODING_TYPE_DEFAULT);

	client = new Client(clientConf);
    }

    @Test(description = "testing the registration of a file")
    public void registering_a_file_should_comply_with_a_process() {
	String fluxname = "fluxTest";
	WatchedFile file = new WatchedFile(fluxname + "_stampTest.test");
	WatchedFile fileWithSameFlux = new WatchedFile("noFlux");

	FluxServiceImpl fluxService = mock(FluxServiceImpl.class);
	fileService.fluxService = fluxService;
	FileHelper fileHelper = mock(FileHelper.class);
	fileService.fileHelper = fileHelper;

	when(fluxService.getFluxName(anyString(), anyString(), anyString())).thenReturn(fluxname);
	doCallRealMethod().when(fluxService).addFlux(anyString(), any(Client.class));
	doCallRealMethod().when(fluxService).addFileToFlux(anyString(), any(), any(Client.class));
	when(fileHelper.getSeparator(client)).thenReturn(SEPARATOR_DEFAULT);
	when(fileHelper.getStampPosition(client)).thenReturn(STAMP_POSITION_DEFAULT);

	fileService.registerFile(client, file);
	assertTrue(client.getWatchedFilesByFlux().keySet().contains(fluxname), "flux not registered");
	assertTrue(client.getWatchedFilesByFlux().get(fluxname).contains(file),
		"file not included in the list related to the flux");

	fileService.registerFile(client, fileWithSameFlux);
	assertEquals(client.getWatchedFilesByFlux().keySet().size(), 1, "incorrect amont of flux registered");
	assertEquals(client.getWatchedFilesByFlux().get(fluxname).size(), 2,
		"incorrect amont of files registered in a flux");

	client.getWatchedFilesByFlux().get(fluxname).stream()
		.forEach(fileTest -> assertTrue(fileTest.isRegistered(), "file wasn't set as registered"));
    }

    @Test(description = "we only have a small test because the newFileProcess method mainly calls other methods which are tested elsewhere")
    public void the_process_of_a_new_file_should_comply_with_a_process() {
	String filename = "fluxTest_stampTest.test";
	client.getConf().setWorkingDir("src/test/resources");

	FluxServiceImpl fluxService = mock(FluxServiceImpl.class);
	fileService.fluxService = fluxService;
	FileHelper fileHelper = mock(FileHelper.class);
	fileService.fileHelper = fileHelper;
	FolderServiceImpl mover = mock(FolderServiceImpl.class);
	fileService.mover = mover;
	HashServiceImpl hasher = mock(HashServiceImpl.class);
	fileService.hasher = hasher;
	LogChainerServiceImpl chainer = mock(LogChainerServiceImpl.class);
	fileService.chainer = chainer;

	when(fluxService.getFluxName(anyString(), anyString(), anyString())).thenReturn("noFlux");
	when(fileHelper.getSeparator(any(Client.class))).thenReturn("noSeparator");

	when(mover.moveFileInDirWithNoSameNameFile(anyString(), anyString(), anyString())).thenReturn(null);

	when(hasher.getPreviousFileHash(any())).thenReturn(new byte[] {});
	when(fileHelper.getEncodingType(any(Client.class))).thenReturn(ENCODING_TYPE_DEFAULT);
	when(fileHelper.getStampPosition(any(Client.class))).thenReturn(STAMP_POSITION_DEFAULT);

	doNothing().when(chainer).chainingLogFile(anyString(), anyInt(), any());

	when(mover.copyFileToDirByReplacingExisting(anyString(), anyString(), anyString())).thenReturn(null);

	fileService.newFileProcess(client, filename);

	verify(mover).copyFileToDirByReplacingExisting(any(), any(), any());
    }

    @Test(description = "testing the way of sorting the files using their stamp")
    public void sorting_the_files_should_comply_with_a_process() {
	FluxServiceImpl fluxService = mock(FluxServiceImpl.class);
	fileService.fluxService = fluxService;
	when(fluxService.getSortingStamp(anyString(), anyString(), anyString())).thenCallRealMethod();

	List<WatchedFile> filesNumericalStampRefList = new ArrayList<>();
	filesNumericalStampRefList.add(new WatchedFile("fluxTest1_001.txt"));
	filesNumericalStampRefList.add(new WatchedFile("fluxTest1_002.txt"));
	filesNumericalStampRefList.add(new WatchedFile("fluxTest1_003.txt"));
	filesNumericalStampRefList.add(new WatchedFile("fluxTest1_011.txt"));
	filesNumericalStampRefList.add(new WatchedFile("fluxTest1_021.txt"));
	filesNumericalStampRefList.add(new WatchedFile("fluxTest1_032.txt"));
	filesNumericalStampRefList.add(new WatchedFile("fluxTest1_101.txt"));
	filesNumericalStampRefList.add(new WatchedFile("fluxTest1_212.txt"));
	filesNumericalStampRefList.add(new WatchedFile("fluxTest1_323.txt"));

	List<WatchedFile> filesNumericalStamp = new ArrayList<>();
	filesNumericalStamp.add(new WatchedFile("fluxTest1_323.txt"));
	filesNumericalStamp.add(new WatchedFile("fluxTest1_032.txt"));
	filesNumericalStamp.add(new WatchedFile("fluxTest1_011.txt"));
	filesNumericalStamp.add(new WatchedFile("fluxTest1_212.txt"));
	filesNumericalStamp.add(new WatchedFile("fluxTest1_021.txt"));
	filesNumericalStamp.add(new WatchedFile("fluxTest1_002.txt"));
	filesNumericalStamp.add(new WatchedFile("fluxTest1_001.txt"));
	filesNumericalStamp.add(new WatchedFile("fluxTest1_003.txt"));
	filesNumericalStamp.add(new WatchedFile("fluxTest1_101.txt"));

	fileService.sortFiles(SEPARATOR_DEFAULT, SORT_DEFAULT, STAMP_POSITION_DEFAULT, filesNumericalStamp);
	assertEquals(filesNumericalStamp.size(), filesNumericalStampRefList.size(),
		"incorrect amount of files returned by the sorting method for a numerical stamp");
	for (int i = 0; i < filesNumericalStamp.size(); ++i) {
	    assertEquals(filesNumericalStamp.get(i).getFilename(), filesNumericalStampRefList.get(i).getFilename(),
		    "incorrect order for the numerical stamp");
	}

	List<WatchedFile> filesAlphabeticalStampRefList = new ArrayList<>();
	filesAlphabeticalStampRefList.add(new WatchedFile("fluxTest1_aaa.txt"));
	filesAlphabeticalStampRefList.add(new WatchedFile("fluxTest1_aab.txt"));
	filesAlphabeticalStampRefList.add(new WatchedFile("fluxTest1_aar.txt"));
	filesAlphabeticalStampRefList.add(new WatchedFile("fluxTest1_act.txt"));
	filesAlphabeticalStampRefList.add(new WatchedFile("fluxTest1_acv.txt"));
	filesAlphabeticalStampRefList.add(new WatchedFile("fluxTest1_afg.txt"));
	filesAlphabeticalStampRefList.add(new WatchedFile("fluxTest1_cad.txt"));
	filesAlphabeticalStampRefList.add(new WatchedFile("fluxTest1_cae.txt"));
	filesAlphabeticalStampRefList.add(new WatchedFile("fluxTest1_hra.txt"));

	List<WatchedFile> filesAlphabeticalStamp = new ArrayList<>();
	filesAlphabeticalStamp.add(new WatchedFile("fluxTest1_aab.txt"));
	filesAlphabeticalStamp.add(new WatchedFile("fluxTest1_act.txt"));
	filesAlphabeticalStamp.add(new WatchedFile("fluxTest1_afg.txt"));
	filesAlphabeticalStamp.add(new WatchedFile("fluxTest1_cad.txt"));
	filesAlphabeticalStamp.add(new WatchedFile("fluxTest1_hra.txt"));
	filesAlphabeticalStamp.add(new WatchedFile("fluxTest1_cae.txt"));
	filesAlphabeticalStamp.add(new WatchedFile("fluxTest1_aar.txt"));
	filesAlphabeticalStamp.add(new WatchedFile("fluxTest1_acv.txt"));
	filesAlphabeticalStamp.add(new WatchedFile("fluxTest1_aaa.txt"));

	fileService.sortFiles(SEPARATOR_DEFAULT, "alphabetical", STAMP_POSITION_DEFAULT, filesAlphabeticalStamp);
	assertEquals(filesAlphabeticalStamp.size(), filesAlphabeticalStampRefList.size(),
		"incorrect amount of files returned by the sorting method for a alphabetical stamp");
	for (int i = 0; i < filesAlphabeticalStamp.size(); ++i) {
	    assertEquals(filesAlphabeticalStamp.get(i).getFilename(),
		    filesAlphabeticalStampRefList.get(i).getFilename(), "incorrect order for the alphabetical stamp");
	}
    }

    @Test(description = "testing the message to be inserted")
    public void the_message_to_insert_should_comply_with_a_format() {
	Collection<File> previousFiles = new ArrayList<>();
	byte[] noHash = new byte[] {};

	FileHelper fileHelper = mock(FileHelper.class);
	fileService.fileHelper = fileHelper;

	when(fileHelper.getEncodingType(any(Client.class))).thenReturn(ENCODING_TYPE_DEFAULT);

	// testing for a normal message with previous file
	previousFiles.add(new File("testPreviousFilename"));
	String messageTestWithPreviousFile = fileService.messageToInsert(noHash, previousFiles, client);
	assertTrue(messageTestWithPreviousFile.contains(" testPreviousFilename\n"),
		"message to insert doesn't contain ' testPreviousFilename\n'");
	assertTrue(messageTestWithPreviousFile.contains("SHA-256:"),
		"message to insert doesn't contain 'SHA-256:'");

	// invalid charset
	when(fileHelper.getEncodingType(any(Client.class))).thenReturn("Invalid_charset");
	try {
	    fileService.messageToInsert(noHash, previousFiles, client);
	} catch (BusinessException e) {
	    assertEquals(e.getCause().getClass(), UnsupportedEncodingException.class,
		    "UnsupportedEncodingException wasn't detected");
	}
    }
}
