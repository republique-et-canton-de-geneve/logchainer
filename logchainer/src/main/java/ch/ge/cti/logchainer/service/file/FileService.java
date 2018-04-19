package ch.ge.cti.logchainer.service.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.FileWatched;

public interface FileService {
    /**
     * Register the file and relates it with it's client.
     * 
     * @param client
     * @param file
     */
    void registerFile(Client client, FileWatched file);

    /**
     * Treatment of the file as an entry create.
     * 
     * @param clientNb
     * @param filename
     */
    void newFileTreatment(Client client, String filename);

    /**
     * Sort the files of a specified flux using a specified sorting type.
     * 
     * @param separator
     * @param sorter
     * @param files
     */
    void sortFiles(String separator, String sorter, ArrayList<FileWatched> files);

    /**
     * Get the collection of all already existing same flux files in tmp
     * directory. (Should be only one)
     * 
     * @param fluxName
     * @return collection of these files
     */
    Collection<File> getPreviousFiles(String fluxName, String workingDir, String separator);
}
