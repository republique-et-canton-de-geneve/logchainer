package ch.ge.cti.logchainer.service.file;

import static org.testng.Assert.*;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;
import ch.ge.cti.logchainer.constante.LogChainerConstante;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.FilePattern;

public class FileServiceTest {
    private Client client;
    private final FileService fileService = new FileServiceImpl();

    @BeforeTest
    public void setUp() {
	ClientConf clientConfCustom = new ClientConf();
	FilePattern filePatternCustom = new FilePattern();

	filePatternCustom.setSeparator(LogChainerConstante.SEPARATOR_DEFAULT);
	filePatternCustom.setSortingType(LogChainerConstante.SORT_DEFAULT);

	clientConfCustom.setFilePattern(filePatternCustom);
	clientConfCustom.setClientId("ClientTest");
	clientConfCustom.setFileEncoding(LogChainerConstante.ENCODING_TYPE_DEFAULT);

	client = new Client(clientConfCustom);
    }
    
    @Test(description = "testing the registration of a file")
    public void testRegisterFile() {
	String fluxname = "fluxTest";
	FileWatched file = new FileWatched(fluxname + "_stampTest.test");
	
	fileService.registerFile(client, file);
	assertTrue(client.getFluxFileMap().keySet().contains(fluxname));
	assertTrue(client.getFluxFileMap().get(fluxname).contains(file));
    }
}
