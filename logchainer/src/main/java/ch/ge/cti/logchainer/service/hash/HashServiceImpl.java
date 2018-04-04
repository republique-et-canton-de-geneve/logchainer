package ch.ge.cti.logchainer.service.hash;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HashServiceImpl implements HashService {

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(HashServiceImpl.class.getName());

    @Override
    public byte[] getLogHashCode(InputStream fileStream) throws IOException {
	LOG.debug("Hashing algorithm job");
	return DigestUtils.sha256(fileStream);
    }

    @Override
    public byte[] getNullHash() {
	LOG.debug("null hash method job");
	return new byte[] {};
    }
}
