package ch.ge.cti.logchainer.service.flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.service.file.FileServiceImpl;
import ch.ge.cti.logchainer.service.folder.FolderServiceImpl;
import ch.ge.cti.logchainer.service.logwatcher.LogWatcherServiceImpl;
import ch.ge.cti.logchainer.service.utils.UtilsComponentsImpl;

public class FluxServiceTest {
    private final FluxServiceImpl fluxService = new FluxServiceImpl();
    private final String testFilename = "fluxTest_stampTest.txt";
    private ArrayList<FileWatched> watchedFiles = new ArrayList<>();
    private Map<String, ArrayList<FileWatched>> mapFluxFiles;

    @BeforeTest
    public void setUp() {
	watchedFiles.add(new FileWatched("file1"));
	watchedFiles.add(new FileWatched("file2"));
	watchedFiles.add(new FileWatched("file3"));
	watchedFiles.add(new FileWatched("file4"));

	mapFluxFiles = new HashMap<>();
    }

    @Test(description = "testing the method getting the flux name")
    public void testGetFluxName() {
	String fluxname = fluxService.getFluxName(testFilename, "_");

	assertEquals(fluxname, "fluxTest");
    }

    @Test(description = "testing the method getting the stamp used to sort files")
    public void testGetSortingStamp() {
	String stamp = fluxService.getSortingStamp(testFilename, "_");

	assertEquals(stamp, "stampTest");
    }

    @Test(description = "testing when the is ready to be treated")
    public void testIsFluxReadyToBeTreated() {
	watchedFiles.stream().forEach(file -> file.setReadyToBeTreated(true));
	mapFluxFiles.put("fluxTest1", watchedFiles);
	mapFluxFiles.entrySet().stream().forEach(flux -> assertTrue(fluxService.isFluxReadyToBeTreated(flux)));
	mapFluxFiles.clear();

	ArrayList<FileWatched> nonReadyWatchedFiles = new ArrayList<>();
	nonReadyWatchedFiles.add(new FileWatched("file1"));
	nonReadyWatchedFiles.add(new FileWatched("file2"));
	nonReadyWatchedFiles.get(1).setReadyToBeTreated(true);
	mapFluxFiles.put("fluxTest2", nonReadyWatchedFiles);
	mapFluxFiles.entrySet().stream().forEach(flux -> assertFalse(fluxService.isFluxReadyToBeTreated(flux)));
    }

    @Test(description = "testing the treatment of the flux")
    public void testFluxTreatment() {
	LogWatcherServiceImpl watcherService = mock(LogWatcherServiceImpl.class);
	fluxService.watcherService = watcherService;
	FileServiceImpl fileService = mock(FileServiceImpl.class);
	fluxService.fileService = fileService;
	UtilsComponentsImpl component = mock(UtilsComponentsImpl.class);
	fluxService.component = component;

	Client client = mock(Client.class);

	List<String> refList = new ArrayList<>();
	refList.add("fluxTest1");
	refList.add("fluxTest2");
	List<String> fluxList = new ArrayList<>();

	mapFluxFiles.put("fluxTest1", watchedFiles);
	mapFluxFiles.put("fluxTest2", watchedFiles);

	when(watcherService.treatmentAfterDetectionOfEvent(any(Client.class), anyString(), any())).thenReturn(true);
	doNothing().when(fileService).sortFiles(anyString(), anyString(), any());
	when(component.getSeparator(client)).thenReturn(null);
	when(component.getSorter(client)).thenReturn(null);

	mapFluxFiles.entrySet().stream().forEach(fluxname -> fluxService.fluxTreatment(client, fluxList, fluxname));

	assertEquals(fluxList.size(), refList.size());
	fluxList.stream().forEach(flux -> assertTrue(refList.contains(flux)));
    }

    @Test(description = "testing the process of a corrupted file")
    public void testCorruptedFluxProcess() {
	LogWatcherServiceImpl watcherService = mock(LogWatcherServiceImpl.class);
	fluxService.watcherService = watcherService;
	FileServiceImpl fileService = mock(FileServiceImpl.class);
	fluxService.fileService = fileService;
	UtilsComponentsImpl component = mock(UtilsComponentsImpl.class);
	fluxService.component = component;
	FolderServiceImpl mover = mock(FolderServiceImpl.class);
	fluxService.mover = mover;

	ClientConf clientConf = new ClientConf();
	clientConf.setInputDir(null);
	clientConf.setCorruptedFilesDir(null);

	Client client = new Client(clientConf);

	List<String> refList = new ArrayList<>();
	refList.add("fluxTest1");
	refList.add("fluxTest2");
	List<String> fluxList = new ArrayList<>();

	mapFluxFiles.put("fluxTest1", watchedFiles);
	mapFluxFiles.put("fluxTest2", watchedFiles);

	when(mover.moveFileInDirWithNoSameNameFile(anyString(), anyString(), anyString())).thenReturn(null);

	mapFluxFiles.entrySet().stream()
		.forEach(fluxname -> fluxService.corruptedFluxProcess(client, fluxList, fluxname));

	assertEquals(fluxList.size(), refList.size());
	fluxList.stream().forEach(flux -> assertTrue(refList.contains(flux)));
    }
}
