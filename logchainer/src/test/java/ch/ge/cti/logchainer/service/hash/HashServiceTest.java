package ch.ge.cti.logchainer.service.hash;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.testng.annotations.Test;

import ch.ge.cti.logchainer.exception.BusinessException;

public class HashServiceTest {
    private final HashService hasher = new HashServiceImpl();

    @Test(description = "hash method test")
    public void testHashCode() throws IOException {
	final String pathTestFile = "src/test/resources/testHashCode";
	final byte[] refArray = new byte[] { 14, -39, -87, 75, 76, -6, -127, -30, 51, 126, -4, 21, 5, 102, -98, 25, 100,
		-62, 91, -19, 117, 1, 50, 118, -89, 57, -10, 11, -8, -49, 15, -18 };

	try (InputStream fileToTest = new FileInputStream(new File(pathTestFile))) {
	    hasher.getNullHash();
	    assertEquals(hasher.getLogHashCode(fileToTest), refArray);
	}
    }

    @Test(description = "null hash method test")
    public void testNullHash() {
	byte[] nullHash = new byte[] {};

	assertEquals(hasher.getNullHash(), nullHash);
    }

    @Test(description = "testing the method for getting the hashCode of files from collection (should be 1 file)")
    public void testGetPreviousHash() throws IOException {
	Collection<File> previousFiles = new ArrayList<>();
	String data = "testing the hash method";
	Files.write(Paths.get("src/test/resources/hashForTest"), data.getBytes());
	File hashForTest = new File("src/test/resources/hashForTest");

	// test when a valid previous file has been found
	previousFiles.add(hashForTest);
	byte[] codeHashForTest;
	try (InputStream stream = new FileInputStream(hashForTest)) {
	    codeHashForTest = hasher.getLogHashCode(stream);
	}

	assertEquals(hasher.getPreviousFileHash(previousFiles), codeHashForTest);

	// test when no previous file have been found
	previousFiles.clear();
	assertEquals(hasher.getPreviousFileHash(previousFiles), hasher.getNullHash());

	// test when an invalid previous file has been provided
	previousFiles.add(new File("nonExistingFile"));
	try {
	    hasher.getPreviousFileHash(previousFiles);
	} catch (BusinessException e) {
	    assertEquals(e.getCause().getClass(), FileNotFoundException.class);
	}
    }
}
