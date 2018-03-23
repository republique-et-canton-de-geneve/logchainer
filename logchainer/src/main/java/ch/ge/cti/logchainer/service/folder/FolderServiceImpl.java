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
	LOG.info("file moving method entered");

	Files.move(Paths.get(pathInput + "/" + pathFile), Paths.get(pathTmp + "/" + pathFile), new CopyOption[] {});

	LOG.info("file successfully moved to directory : {0}/{1} ", pathTmp, pathFile);

	return pathTmp + "/" + pathFile;
    }

    @Override
    public String moveFileTmpToOutput(String pathFile, String pathTmp, String pathOutput) throws IOException {
	LOG.info("file moving method entered");

	Files.copy(Paths.get(pathTmp + "/" + pathFile), Paths.get(pathOutput + "/" + pathFile),
		StandardCopyOption.REPLACE_EXISTING);

	LOG.info(
		"file successfully moved to directory : {0}/{1}, replacing file if one with same name was already existing",
		pathOutput, pathFile);

	return pathOutput + "/" + pathFile;
    }

}
