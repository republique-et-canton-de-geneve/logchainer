package ch.ge.cti.logchainer.service;

import java.io.IOException;

public interface FolderService {
    /**
     * Moves indicated file from the input Directory to the tmp one.
     * 
     * @param pathFile
     * @param pathDir
     * @param pathTmp
     * @return file's path once moved
     * @throws IOException
     */
    String moveFileInputToTmp(String pathFile, String pathInput, String pathTmp) throws IOException;

    /**
     * Moves indicated file from the tmp Directory to the output one.
     * 
     * @param pathFile
     * @param pathTmp
     * @param pathOutput
     * @return file's path once moved
     * @throws IOException
     */
    String moveFileTmpToOutput(String pathFile, String pathTmp, String pathOutput) throws IOException;
}
