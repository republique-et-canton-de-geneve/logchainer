package ch.ge.cti.logchainer.service;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.testng.annotations.Test;

public class HashServiceTest {

    @Test(description = "hash method test")
    public void testHashCode() throws IOException {
	String pathTestFile = "testHashCode";
	byte[] refArray = new byte[] { 14, -39, -87, 75, 76, -6, -127, -30, 51, 126, -4, 21, 5, 102, -98, 25, 100, -62,
		91, -19, 117, 1, 50, 118, -89, 57, -10, 11, -8, -49, 15, -18 };

	try (InputStream fileToTest = this.getClass().getClassLoader().getResourceAsStream(pathTestFile)) {
	    assertEquals(HashService.getLogHashCode(fileToTest), refArray);
	}
    }

}
