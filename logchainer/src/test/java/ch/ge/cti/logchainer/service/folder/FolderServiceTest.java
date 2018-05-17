package ch.ge.cti.logchainer.service.folder;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.exception.BusinessException;

public class FolderServiceTest {
    private final String testResourcesDirPath = "src/test/resources";
    private final FolderService mover = new FolderServiceImpl();
    private static final String filename1 = "testMovingFile1.txt";
    private static final String filename2 = "testMovingFile2.txt";

    @Test(description = "testing the move of files")
    public void testMovingFile() throws Exception {
	String noData = "";
	Files.write(Paths.get(testResourcesDirPath + "/" + filename1), noData.getBytes());
	Files.write(Paths.get(testResourcesDirPath + "/" + filename2), noData.getBytes());

	mover.moveFileInDirWithNoSameNameFile(filename1, testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	mover.moveFileInDirWithNoSameNameFile(filename2, testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	Collection<File> existingFilesMoved = getPreviousFiles(testResourcesDirPath + "/testMovingToFolder");

	assertEquals(existingFilesMoved.contains(new File(testResourcesDirPath + "/testMovingToFolder/" + filename1)),
		true);
	assertEquals(existingFilesMoved.contains(new File(testResourcesDirPath + "/testMovingToFolder/" + filename2)),
		true);

	Files.write(Paths.get(testResourcesDirPath + "/" + filename1), noData.getBytes());
	try {
	    mover.moveFileInDirWithNoSameNameFile(filename1, testResourcesDirPath,
		    testResourcesDirPath + "/testMovingToFolder");
	} catch (BusinessException e) {
	    assertEquals(e.getCause().getClass(), FileAlreadyExistsException.class);
	}

	try {
	    mover.moveFileInDirWithNoSameNameFile("nonExistingFile", testResourcesDirPath,
		    testResourcesDirPath + "/testMovingToFolder");
	} catch (BusinessException e) {
	    assertEquals(e.getCause().getClass(), NoSuchFileException.class);
	}

	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/" + filename1));
	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/" + filename2));
	Files.delete(Paths.get(testResourcesDirPath + "/" + filename1));
    }

    @Test(description = "testing the copy of files")
    public void testCopyingFile() throws IOException {
	String noData = "";
	Files.write(Paths.get(testResourcesDirPath + "/" + filename1), noData.getBytes());
	Files.write(Paths.get(testResourcesDirPath + "/" + filename2), noData.getBytes());

	mover.copyFileToDirByReplacingExisting(filename1, testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	mover.copyFileToDirByReplacingExisting(filename2, testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	Collection<File> existingFilesCopied = getPreviousFiles(testResourcesDirPath + "/testMovingToFolder");
	assertTrue(existingFilesCopied.contains(new File(testResourcesDirPath + "/testMovingToFolder/" + filename1)));
	assertTrue(existingFilesCopied.contains(new File(testResourcesDirPath + "/testMovingToFolder/" + filename2)));

	Collection<File> existingFilesStaying = getPreviousFiles(testResourcesDirPath);
	assertTrue(existingFilesStaying.contains(new File(testResourcesDirPath + "/" + filename1)));
	assertTrue(existingFilesStaying.contains(new File(testResourcesDirPath + "/" + filename2)));

	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/" + filename1));
	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/" + filename2));

	Files.delete(Paths.get(testResourcesDirPath + "/" + filename1));
	Files.delete(Paths.get(testResourcesDirPath + "/" + filename2));
    }

    @SuppressWarnings("unchecked")
    private Collection<File> getPreviousFiles(String workingDir) {
	// filtering the files to only keep the same as given flux one (should
	// be unique)
	return FileUtils.listFiles(new File(workingDir), new IOFileFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return accept(new File(name));
	    }

	    @Override
	    public boolean accept(File file) {
		return true;
	    }
	}, null);
    }
}
