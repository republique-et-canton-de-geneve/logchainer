package ch.ge.cti.logchainer.service.hashService;

import java.io.IOException;
import java.io.InputStream;

public interface HashService {
    /**
     * Computes the SHA256 HashCode of the given stream.
     * 
     * @param fileStream
     * @return the result of the hash algorithm
     * @throws IOException
     */
    byte[] getLogHashCode(InputStream fileStream) throws IOException;

    /**
     * To get a HashCode for a null object (defined by programmer)
     * 
     * @return empty byte array (arbitrary choice that can be changed)
     */
    byte[] getNullHash();
}
