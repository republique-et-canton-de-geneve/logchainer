package ch.ge.cti.logchainer.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class LogChainerService {

    public void chainingLogFile(String filename, long offset, byte[] content) throws IOException {
	RandomAccessFile r = new RandomAccessFile(new File(filename), "rw");
	File tempFile = new File(filename + "~");
	RandomAccessFile rtemp = new RandomAccessFile(tempFile, "rw");
	long fileSize = r.length();
	FileChannel sourceChannel = r.getChannel();
	FileChannel targetChannel = rtemp.getChannel();
	sourceChannel.transferTo(offset, (fileSize - offset), targetChannel);
	sourceChannel.truncate(offset);
	r.seek(offset);
	r.write(content);
	long newOffset = r.getFilePointer();
	targetChannel.position(0L);
	sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - offset));
	sourceChannel.close();
	targetChannel.close();
	rtemp.close();
	r.close();
	tempFile.delete();
    }
}
