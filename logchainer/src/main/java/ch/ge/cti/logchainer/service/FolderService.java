package ch.ge.cti.logchainer.service;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FolderService {

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FolderService.class.getName());

    private FolderService() {
    }

    /**
     * Moves the newly created files (those who normally don't override any file
     * name in the tmp directory)
     * 
     * @param pFile
     * @throws IOException
     */
    public static String moveFileInputToTmp(String pFile, String pDir, String pTmp) throws IOException {
	LOG.debug("file moving method entered");

	Files.move(Paths.get(pDir + "/" + pFile), Paths.get(pTmp + "/" + pFile), new CopyOption[] {});

	LOG.debug("file successfully moved to directory : " + pTmp + "/" + pFile);

	return pTmp + "/" + pFile;
    }
    
    
    public static String moveFileTmpToOutput(String pTmp, String pFile, String pOutput) throws IOException {
	LOG.info("file moving method entered");
	
	Files.copy(Paths.get(pTmp + "/" + pFile), Paths.get(pOutput + "/" + pFile), StandardCopyOption.REPLACE_EXISTING);
	
	LOG.debug("file successfully moved to directory : " + pOutput + "/" + pFile + ", replacing file if one with same name was already existing");
	
	return pOutput + "/" + pFile;
    }

}
