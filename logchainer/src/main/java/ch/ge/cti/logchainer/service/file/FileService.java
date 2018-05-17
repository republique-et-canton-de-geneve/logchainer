package ch.ge.cti.logchainer.service.file;

import java.util.List;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;

public interface FileService {
    /**
     * Register the file and relates it with it's client.
     * 
     * @param client
     * @param file
     */
    void registerFile(Client client, WatchedFile file);

    /**
     * Process of the file as an entry create.
     * 
     * @param clientNb
     * @param filename
     */
    void newFileProcess(Client client, String filename);

    /**
     * Sort the files of a specified flux using a specified sorting type.
     * 
     * @param separator
     * @param sorter
     * @param files
     */
    void sortFiles(String separator, String sorter, String stampPosition, List<WatchedFile> files);
}
