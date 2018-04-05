package ch.ge.cti.logchainer.service.folder;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FolderServiceImpl implements FolderService {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FolderServiceImpl.class.getName());

    @Override
    public String moveFileInputToTmp(String pathFile, String pathInput, String pathTmp) throws IOException {
	LOG.debug("file moving method entered");

	// the target destination can't contain a same name file
	Files.move(Paths.get(pathInput + "/" + pathFile), Paths.get(pathTmp + "/" + pathFile), new CopyOption[] {});

	LOG.debug("file successfully moved to directory : {}/{}", pathTmp, pathFile);

	return pathTmp + "/" + pathFile;
    }

    @Override
    public String moveFileTmpToOutput(String pathFile, String pathTmp, String pathOutput) throws IOException {
	LOG.debug("file moving method entered");

	// if the target destination contains a same name file, it will be
	// replaced
	Files.copy(Paths.get(pathTmp + "/" + pathFile), Paths.get(pathOutput + "/" + pathFile),
		StandardCopyOption.REPLACE_EXISTING);

	LOG.debug(
		"--------------------- file successfully moved to directory : {}/{}, replacing file if one with same name was already existing",
		pathOutput, pathFile);

	return pathOutput + "/" + pathFile;
    }

}
