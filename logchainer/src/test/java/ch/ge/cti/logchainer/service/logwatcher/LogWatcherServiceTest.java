package ch.ge.cti.logchainer.service.logwatcher;

import static ch.ge.cti.logchainer.constante.LogChainerConstante.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.FilePattern;
import ch.ge.cti.logchainer.generate.LogChainerConf;

public class LogWatcherServiceTest {
    private final LogWatcherServiceImpl watcher = new LogWatcherServiceImpl();
    private static String testResourcesDirPath = "src/test/resources/dirCreationDetectionTest";
    private final LogChainerConf clientConfList = new LogChainerConf();

    @BeforeTest
    public void setUp() throws Exception {
	ClientConf clientConf = new ClientConf();
	
	clientConf.setFilePattern(new FilePattern());
	clientConf.setClientId("ClientTest");
	clientConf.setInputDir(testResourcesDirPath);
	
	clientConfList.getListeClientConf().add(clientConf);

    }

    @Test(description = "testing the initialization of the clients")
    public void testInitializeFileWatcherByClient() {
	watcher.initializeFileWatcherByClient(clientConfList);
	assertEquals(watcher.clients.size(), 1);
	assertEquals(watcher.clients.get(0).getConf(), clientConfList.getListeClientConf().get(0));
	assertNotNull(watcher.clients.get(0).getKey());
    }

}
