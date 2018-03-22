package ch.ge.cti.logchainer.service;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.testng.annotations.Test;

public class FolderServiceTest {
    private static String testResourcesDirPath = "src/test/resources";

    // @Autowired
    // private FolderService mover;
    private FolderServiceImpl mover = new FolderServiceImpl();

    @Test
    public void testMovingFile() throws Exception {
	String noData = "";
	Files.write(Paths.get(testResourcesDirPath + "/testMovingFile1.txt"), noData.getBytes());
	Files.write(Paths.get(testResourcesDirPath + "/testMovingFile2.txt"), noData.getBytes());

	mover.moveFileInputToTmp("testMovingFile1.txt", testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	mover.moveFileInputToTmp("testMovingFile2.txt", testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/testMovingFile1.txt"));
	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/testMovingFile2.txt"));
    }

}
