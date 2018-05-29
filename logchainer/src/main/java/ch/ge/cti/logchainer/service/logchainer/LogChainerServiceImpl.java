/*
 * <Log Chainer>
 *
 * Copyright (C) 2018 République et Canton de Genève
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.ge.cti.logchainer.service.logchainer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.exception.BusinessException;

@Service
public class LogChainerServiceImpl implements LogChainerService {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogChainerServiceImpl.class.getName());

    @Override
    public void chainingLogFile(String filename, long offset, byte[] content) {
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
	    accessToTmpFile(offset, content, tempFile, r);
	} catch (FileNotFoundException e) {
	    throw new BusinessException(filename, e);
	} catch (IOException e) {
	    throw new BusinessException(e);
	}
	// delete the temporary file so that it doesn't appear in the directory
	try {
	    Files.delete(tempFile.toPath());
	    LOG.debug("temp file deleted");
	} catch (IOException e) {
	    throw new BusinessException("temp file could not be deleted", e);
	}

	LOG.info("log chaining completed for file {}", filename);
    }

    /**
     * Access the the tmp file.
     * 
     * @param offset
     * @param content
     * @param tempFile
     * @param r
     */
    private void accessToTmpFile(long offset, byte[] content, File tempFile, RandomAccessFile r) {
	try (RandomAccessFile rtemp = new RandomAccessFile(tempFile, "rw")) {
	    LOG.debug("temporary file accessed as a readable and writable stream");
	    insertionOfMessage(offset, content, r, rtemp);
	} catch (FileNotFoundException e) {
	    throw new BusinessException(tempFile.getName(), e);
	} catch (IOException e) {
	    throw new BusinessException(e);
	}
    }

    /**
     * Insertion of the message into the file.
     * 
     * @param offset
     * @param content
     * @param r
     * @param rtemp
     */
    private void insertionOfMessage(long offset, byte[] content, RandomAccessFile r, RandomAccessFile rtemp) {
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
	} catch (IOException e) {
	    throw new BusinessException(e);
	}
    }
}
