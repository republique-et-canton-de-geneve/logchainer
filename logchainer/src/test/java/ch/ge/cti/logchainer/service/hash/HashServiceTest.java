/*
 * <Log Chainer>
 *
 * Copyright (C) 2018 République et Canton de Genève
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
    public void getting_an_hashcode_should_comply_with_a_process() throws IOException {
	final String pathTestFile = "src/test/resources/testHashCode";
	final byte[] refArray = new byte[] { 14, -39, -87, 75, 76, -6, -127, -30, 51, 126, -4, 21, 5, 102, -98, 25, 100,
		-62, 91, -19, 117, 1, 50, 118, -89, 57, -10, 11, -8, -49, 15, -18 };

	try (InputStream fileToTest = new FileInputStream(new File(pathTestFile))) {
	    hasher.getNullHash();
	    assertEquals(hasher.getLogHashCode(fileToTest), refArray, "wrong hash code");
	}
    }

    @Test(description = "null hash method test")
    public void a_null_hashcode_should_comply_with_convention() {
	byte[] nullHash = new byte[] {};

	assertEquals(hasher.getNullHash(), nullHash, " wrong null hash code");
    }

    @Test(description = "testing the method for getting the hashCode of files from collection (should be 1 file)")
    public void getting_the_hashcode_of_a_previous_file_should_comply_with_a_process() throws IOException {
	Collection<File> previousFiles = new ArrayList<>();
	String data = "testing the hash method";
	Files.write(Paths.get("src/test/resources/hashForTest"), data.getBytes());
	File hashForTest = new File("src/test/resources/hashForTest");

	// test when a valid previous file has been found
	previousFiles.add(hashForTest);
	byte[] hashCodeForTest;
	try (InputStream stream = new FileInputStream(hashForTest)) {
	    hashCodeForTest = hasher.getLogHashCode(stream);
	}

	assertEquals(hasher.getPreviousFileHash(previousFiles), hashCodeForTest, "wrong hash code for previous file");

	// test when no previous file have been found
	previousFiles.clear();
	assertEquals(hasher.getPreviousFileHash(previousFiles), hasher.getNullHash(),
		"wrong null hash code when no previous file");

	// test when an invalid previous file has been provided
	previousFiles.add(new File("nonExistingFile"));
	try {
	    hasher.getPreviousFileHash(previousFiles);
	} catch (BusinessException e) {
	    assertEquals(e.getCause().getClass(), FileNotFoundException.class, "FileNotFoundException wasn't detected");
	}
    }
}
