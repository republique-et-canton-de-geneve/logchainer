package ch.ge.cti.logchainer.service.hash;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.exception.BusinessException;

@Service
public class HashServiceImpl implements HashService {

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(HashServiceImpl.class.getName());

    @Override
    public byte[] getLogHashCode(InputStream fileStream) {
	LOG.info("Hashing algorithm job");
	byte[] hashToReturn = new byte[]{};
	
	try {
	    hashToReturn = DigestUtils.sha256(fileStream);
	} catch (IOException e) {
	    throw new BusinessException("Exception while reading from the stream", e);
	}
	
	return hashToReturn;
    }

    @Override
    public byte[] getNullHash() {
	LOG.info("null hash method job");
	return new byte[] {};
    }
}
