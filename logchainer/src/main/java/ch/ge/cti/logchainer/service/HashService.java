package ch.ge.cti.logchainer.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashService {

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(HashService.class.getName());

    private HashService() {
    }

    public static byte[] getLogHashCode(InputStream fileStream) throws IOException {
	LOG.info("Hashing algorithm entered");

	return DigestUtils.sha256(fileStream);
    }

    public static byte[] getNullHash() {
	LOG.info("null hash method entered");
	return new byte[] {};
    }
}
