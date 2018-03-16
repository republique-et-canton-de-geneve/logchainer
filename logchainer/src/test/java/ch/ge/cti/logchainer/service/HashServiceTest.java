package ch.ge.cti.logchainer.service;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import ch.ge.cti.logchainer.service.HashService;

public class HashServiceTest {

    @Test(description = "hash method test")
    public void testHashCode() {
	String pathTestFile = "D:/_codesource_M501/logchainer-base/logchainer/src/test/resources/testHashCode";
	byte[] refArray = new byte[] { 26, 40, -50, 107, -109, -40, 41, -24, -95, -37, 5, -6, 73, -56, 18, -71, -18,
		-13, 11, 115, -80, -74, 113, 28, -80, 95, 26, 110, 10, -38, 4, -49 };

	assertEquals(HashService.getLogHashCode(pathTestFile), refArray);
    }

}
