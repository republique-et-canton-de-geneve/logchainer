package ch.ge.cti.logchainer.service.folder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.exception.BusinessException;

@Service
public class FolderServiceImpl implements FolderService {
    private static final String FILE_SEPARATOR_CHAR = "/";

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FolderServiceImpl.class.getName());

    @Override
    public String moveFileInDirWithNoSameNameFile(String pathFile, String pathProvidingDir, String pathArrivingDir) {
	LOG.debug("file moving method entered");

	// the target destination can't contain a same name file
	Path fileInInput = Paths.get(pathProvidingDir + FILE_SEPARATOR_CHAR + pathFile);
	Path fileInTmp = Paths.get(pathArrivingDir + FILE_SEPARATOR_CHAR + pathFile);
	try {
	    Files.move(fileInInput, fileInTmp, new CopyOption[] {});
	} catch (FileNotFoundException e) {
	    throw new BusinessException(fileInInput.toString(), e);
	} catch (FileAlreadyExistsException e) {
	    throw new BusinessException(fileInTmp.toString(), e);
	} catch (IOException e) {
	    throw new BusinessException(e);
	}

	if (LOG.isInfoEnabled())
	    LOG.info("file successfully moved to directory : {}", fileInTmp.toString());

	return fileInTmp.toString();
    }

    @Override
    public String copyFileToDirByReplacingExisting(String pathFile, String pathProvidingDir, String pathArrivingDir) {
	LOG.debug("file moving method entered");

	// if the target destination contains a same name file, it will be
	// replaced
	Path fileInTmp = Paths.get(pathProvidingDir + FILE_SEPARATOR_CHAR + pathFile);
	Path fileInOutput = Paths.get(pathArrivingDir + FILE_SEPARATOR_CHAR + pathFile);
	try {
	    Files.copy(fileInTmp, fileInOutput, StandardCopyOption.REPLACE_EXISTING);
	} catch (IOException e) {
	    throw new BusinessException(e);
	}

	if (LOG.isInfoEnabled())
	    LOG.info(
		    "file successfully moved to directory : {}, replacing file if one with same name was already existing",
		    fileInOutput.toString());

	return fileInOutput.toString();
    }

}
