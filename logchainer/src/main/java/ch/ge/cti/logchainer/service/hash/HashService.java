package ch.ge.cti.logchainer.service.hash;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

public interface HashService {
    /**
     * Computes the SHA256 HashCode of the given stream.
     * 
     * @param fileStream
     * @return the result of the hash algorithm
     */
    byte[] getLogHashCode(InputStream fileStream);

    /**
     * To get a HashCode for a null object (defined by programmer)
     * 
     * @return empty byte array (arbitrary choice that can be changed)
     */
    byte[] getNullHash();

    /**
     * Get the hash of the tmp directory's already existing file.
     * 
     * @param previousFiles
     * @return previous file's hashCode
     */
    byte[] getPreviousFileHash(Collection<File> previousFiles);
}
