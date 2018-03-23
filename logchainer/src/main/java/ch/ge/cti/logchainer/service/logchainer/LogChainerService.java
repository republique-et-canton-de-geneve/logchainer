package ch.ge.cti.logchainer.service.logchainer;

import java.io.IOException;

@FunctionalInterface
public interface LogChainerService {
    /**
     * Writes by inserting the byte array 'content' in the file 'filename' where
     * the byte insertion starts at offset.
     * 
     * @param filename
     *            - the name of the file to be written in.
     * @param offset
     *            - where the insertion starts (0 for the beginning)
     * @param content
     *            - message to insert, has to be converted as a byte array
     * @throws IOException
     */
    void chainingLogFile(String filename, long offset, byte[] content) throws IOException;
}
