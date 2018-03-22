package ch.ge.cti.logchainer.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogChainerServiceImpl implements LogChainerService {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogChainerServiceImpl.class.getName());

    @Override
    public void chainingLogFile(String filename, long offset, byte[] content) throws IOException {
	LOG.info("Log chaining method entered");
	File tempFile = new File(filename + "~");
	LOG.info("temporary file created as a new file");

	try (RandomAccessFile r = new RandomAccessFile(new File(filename), "rw")) {
	    LOG.info("file : " + filename + " accessed as a readable and writable stream");
	    try (RandomAccessFile rtemp = new RandomAccessFile(tempFile, "rw")) {
		LOG.info("temporary file accessed a readable and writable stream");
		try (FileChannel sourceChannel = r.getChannel(); FileChannel targetChannel = rtemp.getChannel()) {
		    LOG.info("source channel and target (temporary) channel opened");
		    long fileSize = r.length();

		    sourceChannel.transferTo(offset, (fileSize - offset), targetChannel);
		    LOG.info("post insertion file part transfered to temp channel");

		    sourceChannel.truncate(offset);
		    LOG.info("post insertion file part truncated from original channel");

		    r.seek(offset);
		    r.write(content);
		    LOG.info("content written in the original file's channel");

		    long newOffset = r.getFilePointer();
		    targetChannel.position(0L);
		    sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - offset));
		    LOG.info("post insertion part transfered back from temp channel to original one");
		}
	    }
	}
	tempFile.delete();
	LOG.info("temp file deleted");
    }
}
