package ch.ge.cti.logchainer.service.logchainer;

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
	LOG.debug("Log chaining method entered");
	File tempFile = new File(filename + "~");
	LOG.debug("temporary file created as a new file");

	// Copy the post offset part of the file into another temporary file,
	// then appends the content
	// at the end of the file, finally appends the copied part back to the
	// file from the temporary one
	// at the end of the file (meaning after the added content)
	try (RandomAccessFile r = new RandomAccessFile(new File(filename), "rw")) {
	    LOG.debug("file : {} accessed as a readable and writable stream", filename);
	    try (RandomAccessFile rtemp = new RandomAccessFile(tempFile, "rw")) {
		LOG.debug("temporary file accessed as a readable and writable stream");
		try (FileChannel sourceChannel = r.getChannel(); FileChannel targetChannel = rtemp.getChannel()) {
		    LOG.debug("source channel and target (temporary) channel opened");
		    long fileSize = r.length();

		    sourceChannel.transferTo(offset, (fileSize - offset), targetChannel);
		    LOG.debug("post insertion file part transfered to temp channel");

		    sourceChannel.truncate(offset);
		    LOG.debug("post insertion file part truncated from original channel");

		    r.seek(offset);
		    r.write(content);
		    LOG.debug("content written in the original file's channel");

		    long newOffset = r.getFilePointer();
		    targetChannel.position(0L);
		    sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - offset));
		    LOG.debug("post insertion part transfered back from temp channel to original one");
		}
	    }
	}
	// delete the temporary file so that it doesn't appear in the directory
	if (tempFile.delete())
	    LOG.debug("temp file deleted");
	
	LOG.info("log chaining completed for file {}", filename);
    }
}
