package ch.ge.cti.logchainer.service;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.testng.annotations.Test;

import ch.ge.cti.logchainer.service.folder.FolderServiceImpl;

public class FolderServiceTest {
    private final String testResourcesDirPath = "src/test/resources";
    private final FolderServiceImpl mover = new FolderServiceImpl();

    @Test(description = "testing the move of files")
    public void testMovingFile() throws Exception {
	String noData = "";
	Files.write(Paths.get(testResourcesDirPath + "/testMovingFile1.txt"), noData.getBytes());
	Files.write(Paths.get(testResourcesDirPath + "/testMovingFile2.txt"), noData.getBytes());

	String pathAfterBeingMoved1 = mover.moveFileInDirWithNoSameNameFile("testMovingFile1.txt", testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	String pathAfterBeingMoved2 = mover.moveFileInDirWithNoSameNameFile("testMovingFile2.txt", testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	assertEquals(pathAfterBeingMoved1, "src\\test\\resources\\testMovingToFolder\\testMovingFile1.txt");
	assertEquals(pathAfterBeingMoved2, "src\\test\\resources\\testMovingToFolder\\testMovingFile2.txt");

	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/testMovingFile1.txt"));
	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/testMovingFile2.txt"));
    }

    @Test
    public void testCopyingFile() throws IOException {
	String noData = "";
	Files.write(Paths.get(testResourcesDirPath + "/testMovingFile1.txt"), noData.getBytes());
	Files.write(Paths.get(testResourcesDirPath + "/testMovingFile2.txt"), noData.getBytes());

	String pathAfterBeingMoved1 = mover.copyFileToDirByReplacingExisting("testMovingFile1.txt",
		testResourcesDirPath, testResourcesDirPath + "/testMovingToFolder");

	String pathAfterBeingMoved2 = mover.copyFileToDirByReplacingExisting("testMovingFile2.txt",
		testResourcesDirPath, testResourcesDirPath + "/testMovingToFolder");
	
	assertEquals(pathAfterBeingMoved1, "src\\test\\resources\\testMovingToFolder\\testMovingFile1.txt");
	assertEquals(pathAfterBeingMoved2, "src\\test\\resources\\testMovingToFolder\\testMovingFile2.txt");
    }
}















