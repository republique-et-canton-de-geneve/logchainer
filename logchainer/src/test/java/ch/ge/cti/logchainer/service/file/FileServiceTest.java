package ch.ge.cti.logchainer.service.file;

import static ch.ge.cti.logchainer.constante.LogChainerConstante.ENCODING_TYPE_DEFAULT;
import static ch.ge.cti.logchainer.constante.LogChainerConstante.SEPARATOR_DEFAULT;
import static ch.ge.cti.logchainer.constante.LogChainerConstante.SORT_DEFAULT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;
import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.FilePattern;
import ch.ge.cti.logchainer.service.flux.FluxServiceImpl;
import ch.ge.cti.logchainer.service.folder.FolderServiceImpl;
import ch.ge.cti.logchainer.service.hash.HashServiceImpl;
import ch.ge.cti.logchainer.service.logchainer.LogChainerServiceImpl;
import ch.ge.cti.logchainer.service.utils.UtilsComponentsImpl;

public class FileServiceTest {
    private Client client;
    private final FileServiceImpl fileService = new FileServiceImpl();

    @BeforeTest
    public void setUp() {
	ClientConf clientConfCustom = new ClientConf();
	FilePattern filePatternCustom = new FilePattern();

	filePatternCustom.setSeparator(SEPARATOR_DEFAULT);
	filePatternCustom.setSortingType(SORT_DEFAULT);

	clientConfCustom.setFilePattern(filePatternCustom);
	clientConfCustom.setClientId("ClientTest");
	clientConfCustom.setFileEncoding(ENCODING_TYPE_DEFAULT);

	client = new Client(clientConfCustom);
    }

    @Test(description = "testing the registration of a file")
    public void testRegisterFile() {
	String fluxname = "fluxTest";
	FileWatched file = new FileWatched(fluxname + "_stampTest.test");
	FileWatched fileWithSameFlux = new FileWatched("noFlux");

	FluxServiceImpl fluxService = mock(FluxServiceImpl.class);
	fileService.fluxService = fluxService;
	UtilsComponentsImpl component = mock(UtilsComponentsImpl.class);
	fileService.component = component;

	when(fluxService.getFluxName(anyString(), anyString())).thenReturn(fluxname);
	when(fluxService.isNewFlux(fluxname, client)).thenCallRealMethod();
	doCallRealMethod().when(fluxService).addFlux(fluxname, client);
	doCallRealMethod().when(fluxService).addFileToFlux(fluxname, file, client);
	when(component.getSeparator(client)).thenReturn(SEPARATOR_DEFAULT);

	fileService.registerFile(client, file);
	assertTrue(client.getFluxFileMap().keySet().contains(fluxname));
	assertTrue(client.getFluxFileMap().get(fluxname).contains(file));

	when(fluxService.isNewFlux(fluxname, client)).thenReturn(false);
	doNothing().when(fluxService).addFileToFlux(anyString(), any(FileWatched.class), any(Client.class));

	client.getFluxFileMap().clear();
	fileService.registerFile(client, fileWithSameFlux);
	assertFalse(client.getFluxFileMap().keySet().contains(fluxname));
    }

    @Test(description = "we only have a small test because the newFileTreatment method mainly calls other methods which are tested elsewhere")
    public void testNewFileTreatment() {
	String filename = "fluxTest_stampTest.test";
	client.getConf().setWorkingDir("src/test/resources");

	FluxServiceImpl fluxService = mock(FluxServiceImpl.class);
	fileService.fluxService = fluxService;
	UtilsComponentsImpl component = mock(UtilsComponentsImpl.class);
	fileService.component = component;
	FolderServiceImpl mover = mock(FolderServiceImpl.class);
	fileService.mover = mover;
	HashServiceImpl hasher = mock(HashServiceImpl.class);
	fileService.hasher = hasher;
	LogChainerServiceImpl chainer = mock(LogChainerServiceImpl.class);
	fileService.chainer = chainer;

	when(fluxService.getFluxName(anyString(), anyString())).thenReturn("noFlux");
	when(component.getSeparator(any(Client.class))).thenReturn("noSeparator");

	when(mover.moveFileInDirWithNoSameNameFile(anyString(), anyString(), anyString())).thenReturn(null);

	when(hasher.getPreviousFileHash(any())).thenReturn(new byte[] {});
	when(component.getEncodingType(any(Client.class))).thenReturn("Invalid_charset");

	doNothing().when(chainer).chainingLogFile(anyString(), anyInt(), any(byte[].class));

	when(mover.copyFileToDirByReplacingExisting(anyString(), anyString(), anyString())).thenReturn(null);

	try {
	    fileService.newFileTreatment(client, filename);
	} catch (BusinessException e) {
	    assertEquals(e.getCause().getClass(), UnsupportedEncodingException.class);
	}
    }

    @Test(description = "testing the way of sorting the files using their stamp")
    public void testSortFiles() {
	FluxServiceImpl fluxService = mock(FluxServiceImpl.class);
	fileService.fluxService = fluxService;
	when(fluxService.getSortingStamp(anyString(), anyString())).thenCallRealMethod();

	List<FileWatched> filesNumericalStampRefList = new ArrayList<>();
	filesNumericalStampRefList.add(new FileWatched("fluxTest1_001.txt"));
	filesNumericalStampRefList.add(new FileWatched("fluxTest1_002.txt"));
	filesNumericalStampRefList.add(new FileWatched("fluxTest1_003.txt"));
	filesNumericalStampRefList.add(new FileWatched("fluxTest1_011.txt"));
	filesNumericalStampRefList.add(new FileWatched("fluxTest1_021.txt"));
	filesNumericalStampRefList.add(new FileWatched("fluxTest1_032.txt"));
	filesNumericalStampRefList.add(new FileWatched("fluxTest1_101.txt"));
	filesNumericalStampRefList.add(new FileWatched("fluxTest1_212.txt"));
	filesNumericalStampRefList.add(new FileWatched("fluxTest1_323.txt"));

	List<FileWatched> filesNumericalStamp = new ArrayList<>();
	filesNumericalStamp.add(new FileWatched("fluxTest1_323.txt"));
	filesNumericalStamp.add(new FileWatched("fluxTest1_032.txt"));
	filesNumericalStamp.add(new FileWatched("fluxTest1_011.txt"));
	filesNumericalStamp.add(new FileWatched("fluxTest1_212.txt"));
	filesNumericalStamp.add(new FileWatched("fluxTest1_021.txt"));
	filesNumericalStamp.add(new FileWatched("fluxTest1_002.txt"));
	filesNumericalStamp.add(new FileWatched("fluxTest1_001.txt"));
	filesNumericalStamp.add(new FileWatched("fluxTest1_003.txt"));
	filesNumericalStamp.add(new FileWatched("fluxTest1_101.txt"));

	fileService.sortFiles(SEPARATOR_DEFAULT, SORT_DEFAULT, filesNumericalStamp);
	assertEquals(filesNumericalStamp.size(), filesNumericalStampRefList.size());
	for (int i = 0; i < filesNumericalStamp.size(); ++i) {
	    assertEquals(filesNumericalStamp.get(i).getFilename(), filesNumericalStampRefList.get(i).getFilename());
	}

	List<FileWatched> filesAlphabeticalStampRefList = new ArrayList<>();
	filesAlphabeticalStampRefList.add(new FileWatched("fluxTest1_aaa.txt"));
	filesAlphabeticalStampRefList.add(new FileWatched("fluxTest1_aab.txt"));
	filesAlphabeticalStampRefList.add(new FileWatched("fluxTest1_aar.txt"));
	filesAlphabeticalStampRefList.add(new FileWatched("fluxTest1_act.txt"));
	filesAlphabeticalStampRefList.add(new FileWatched("fluxTest1_acv.txt"));
	filesAlphabeticalStampRefList.add(new FileWatched("fluxTest1_afg.txt"));
	filesAlphabeticalStampRefList.add(new FileWatched("fluxTest1_cad.txt"));
	filesAlphabeticalStampRefList.add(new FileWatched("fluxTest1_cae.txt"));
	filesAlphabeticalStampRefList.add(new FileWatched("fluxTest1_hra.txt"));

	List<FileWatched> filesAlphabeticalStamp = new ArrayList<>();
	filesAlphabeticalStamp.add(new FileWatched("fluxTest1_aab.txt"));
	filesAlphabeticalStamp.add(new FileWatched("fluxTest1_act.txt"));
	filesAlphabeticalStamp.add(new FileWatched("fluxTest1_afg.txt"));
	filesAlphabeticalStamp.add(new FileWatched("fluxTest1_cad.txt"));
	filesAlphabeticalStamp.add(new FileWatched("fluxTest1_hra.txt"));
	filesAlphabeticalStamp.add(new FileWatched("fluxTest1_cae.txt"));
	filesAlphabeticalStamp.add(new FileWatched("fluxTest1_aar.txt"));
	filesAlphabeticalStamp.add(new FileWatched("fluxTest1_acv.txt"));
	filesAlphabeticalStamp.add(new FileWatched("fluxTest1_aaa.txt"));

	fileService.sortFiles(SEPARATOR_DEFAULT, "alphabetical", filesAlphabeticalStamp);
	assertEquals(filesAlphabeticalStamp.size(), filesAlphabeticalStampRefList.size());
	for (int i = 0; i < filesAlphabeticalStamp.size(); ++i) {
	    assertEquals(filesAlphabeticalStamp.get(i).getFilename(),
		    filesAlphabeticalStampRefList.get(i).getFilename());
	}
    }
}
