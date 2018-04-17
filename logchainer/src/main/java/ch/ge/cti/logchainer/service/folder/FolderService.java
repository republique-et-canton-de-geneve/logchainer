package ch.ge.cti.logchainer.service.folder;

public interface FolderService {
    /**
     * Moves indicated file from the input Directory to the tmp one.
     * 
     * @param pathFile
     * @param pathInput
     * @param pathTmp
     * @return file's path once moved
     */
    String moveFileInDirWithNoSameNameFile(String pathFile, String pathInput, String pathTmp);

    /**
     * Moves indicated file from the tmp Directory to the output one.
     * 
     * @param pathFile
     * @param pathTmp
     * @param pathOutput
     * @return file's path once moved
     */
    String copyFileToDirByReplacingExisting(String pathFile, String pathTmp, String pathOutput);
}
