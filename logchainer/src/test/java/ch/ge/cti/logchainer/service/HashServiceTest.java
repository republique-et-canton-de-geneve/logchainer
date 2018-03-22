package ch.ge.cti.logchainer.service;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.testng.annotations.Test;

public class HashServiceTest {
    final static String pathTestFile = "src/test/resources/testHashCode";

    // @Autowired
    // private HashServiceImpl hasher;
    private HashServiceImpl hasher = new HashServiceImpl();

    @Test(description = "hash method test")
    public void testHashCode() throws IOException {
	byte[] refArray = new byte[] { 14, -39, -87, 75, 76, -6, -127, -30, 51, 126, -4, 21, 5, 102, -98, 25, 100, -62,
		91, -19, 117, 1, 50, 118, -89, 57, -10, 11, -8, -49, 15, -18 };

	try (InputStream fileToTest = new FileInputStream(new File(pathTestFile))) {
	    assertEquals(hasher.getLogHashCode(fileToTest), refArray);
	}
    }

}
