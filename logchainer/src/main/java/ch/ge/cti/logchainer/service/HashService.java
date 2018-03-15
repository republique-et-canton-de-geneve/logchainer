package ch.ge.cti.logchainer.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashService {

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory
	    .getLogger(HashService.class.getName());
    
    
    private HashService(){}
    

    public static byte[] getLogHashCode(String pFile) {
	LOG.info("Hashing algorithm entered");

	return DigestUtils.sha256(pFile);
    }
}
