package ch.ge.cti.logchainer.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogChainerService {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogChainerService.class.getName());

    /**
     * Writes by inserting the byte array 'content' in the file 'filename'
     * where the byte insertion starts at offset.
     * @param filename - the name of the file to be written in.
     * @param offset - where the insertion starts (0 for the beginning)
     * @param content - message to insert, has to be converted as a byte array
     * @throws IOException
     */
    public void chainingLogFile(String filename, long offset, byte[] content) throws IOException {
	LOG.info("Log chaining method entered");
	File tempFile = new File(filename + "~");
	LOG.info("temporary file created as a new file");

	try (RandomAccessFile r = new RandomAccessFile(new File(filename), "rw")) {
	    LOG.info("file : " + filename + " accessed as a readable and writable stream");
	    try (RandomAccessFile rtemp = new RandomAccessFile(tempFile, "rw")) {
		LOG.info("temporary file accessed a readable and writable stream");
		try (FileChannel sourceChannel = r.getChannel() ; FileChannel targetChannel = rtemp.getChannel()) {
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
