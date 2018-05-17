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
    public void moving_a_file_should_comply_with_a_process() throws Exception {
	String noData = "";
	Files.write(Paths.get(testResourcesDirPath + "/" + filename1), noData.getBytes());
	Files.write(Paths.get(testResourcesDirPath + "/" + filename2), noData.getBytes());

	mover.moveFileInDirWithNoSameNameFile(filename1, testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	mover.moveFileInDirWithNoSameNameFile(filename2, testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	Collection<File> existingFilesMoved = getPreviousFiles(testResourcesDirPath + "/testMovingToFolder");

	assertTrue(existingFilesMoved.contains(new File(testResourcesDirPath + "/testMovingToFolder/" + filename1)),
		"file 1 wasn't properly moved");
	assertTrue(existingFilesMoved.contains(new File(testResourcesDirPath + "/testMovingToFolder/" + filename2)),
		"file 2 wasn't properly moved");

	Files.write(Paths.get(testResourcesDirPath + "/" + filename1), noData.getBytes());
	try {
	    mover.moveFileInDirWithNoSameNameFile(filename1, testResourcesDirPath,
		    testResourcesDirPath + "/testMovingToFolder");
	} catch (BusinessException e) {
	    assertEquals(e.getCause().getClass(), FileAlreadyExistsException.class,
		    "FileAlreadyExistsException wasn't detected");
	}

	try {
	    mover.moveFileInDirWithNoSameNameFile("nonExistingFile", testResourcesDirPath,
		    testResourcesDirPath + "/testMovingToFolder");
	} catch (BusinessException e) {
	    assertEquals(e.getCause().getClass(), NoSuchFileException.class, "NoSuchFileException wasn't detected");
	}

	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/" + filename1));
	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/" + filename2));
	Files.delete(Paths.get(testResourcesDirPath + "/" + filename1));
    }

    @Test(description = "testing the copy of files")
    public void copying_a_file_should_comply_with_a_process() throws IOException {
	String noData = "";
	Files.write(Paths.get(testResourcesDirPath + "/" + filename1), noData.getBytes());
	Files.write(Paths.get(testResourcesDirPath + "/" + filename2), noData.getBytes());

	mover.copyFileToDirByReplacingExisting(filename1, testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	mover.copyFileToDirByReplacingExisting(filename2, testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	Collection<File> existingFilesCopied = getPreviousFiles(testResourcesDirPath + "/testMovingToFolder");
	assertTrue(existingFilesCopied.contains(new File(testResourcesDirPath + "/testMovingToFolder/" + filename1)),
		"file 1 wsn't properly copied");
	assertTrue(existingFilesCopied.contains(new File(testResourcesDirPath + "/testMovingToFolder/" + filename2)),
		"file 2 wasn't properly copied");

	Collection<File> existingFilesStaying = getPreviousFiles(testResourcesDirPath);
	assertTrue(existingFilesStaying.contains(new File(testResourcesDirPath + "/" + filename1)),
		"file 1 has been removed from original place");
	assertTrue(existingFilesStaying.contains(new File(testResourcesDirPath + "/" + filename2)),
		"file 2 has been removed from original place");

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
