package ch.ge.cti.logchainer.service.hash;

import java.io.InputStream;

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
}
