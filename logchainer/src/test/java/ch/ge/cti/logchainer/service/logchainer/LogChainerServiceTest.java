package ch.ge.cti.logchainer.service.logchainer;

import static org.testng.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.annotations.Test;

import ch.ge.cti.logchainer.service.hash.HashServiceImpl;

public class LogChainerServiceTest {
    private final String testResourcesPath = "src/test/resources";
    private final LogChainerService chainer = new LogChainerServiceImpl();

    @Test(description = "testing the writting in a file")
    public void writing_in_a_file_should_comply_with_a_process() throws IOException {
	String noData = "";
	Path filename = Files.write(Paths.get(testResourcesPath + "/testWriteInFile.txt"), noData.getBytes());
	String textToWrite = "test is a success";
	byte[] result = new byte[] { -85, -71, -67, 115, -94, -25, -30, -99, -38, 59, -79, -78, -70, 78, 89, -102, -86,
		-75, -78, -2, 88, 121, 54, 61, 51, 87, 11, -36, -54, 116, -53, -3 };
	HashServiceImpl hasher = new HashServiceImpl();

	chainer.chainingLogFile(filename.toString(), 0, textToWrite.getBytes());
	try (FileInputStream is = new FileInputStream(filename.toFile())) {
	    assertEquals(hasher.getLogHashCode(is), result);
	}

	Files.delete(Paths.get(testResourcesPath + "/testWriteInFile.txt"));
    }
}
