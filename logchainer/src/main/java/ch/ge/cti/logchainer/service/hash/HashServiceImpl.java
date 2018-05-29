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

package ch.ge.cti.logchainer.service.hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Optional;

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
	byte[] hashToReturn = new byte[] {};

	try {
	    hashToReturn = DigestUtils.sha256(fileStream);
	} catch (IOException e) {
	    throw new BusinessException(e);
	}

	return hashToReturn;
    }

    @Override
    public byte[] getNullHash() {
	LOG.info("null hash method job");
	return new byte[] {};
    }

    @Override
    public byte[] getPreviousFileHash(Collection<File> previousFiles) {
	byte[] hashCodeOfLog;
	// hash the provided file, with null hash if there's no file (=no
	// previous same flux file)
	Optional<File> previousFirstFile = previousFiles.stream().findFirst();
	if (previousFirstFile.isPresent()) {
	    File previousFile = previousFirstFile.get();
	    try (InputStream is = new FileInputStream((previousFile))) {
		LOG.debug("inputStream of the previous file opened");
		hashCodeOfLog = getLogHashCode(is);
	    } catch (FileNotFoundException e) {
		throw new BusinessException(previousFile.getName(), e);
	    } catch (IOException e) {
		throw new BusinessException(e);
	    }
	    LOG.debug("previous file name is : {}", previousFile.getName());

	    try {
		Files.delete(previousFile.toPath());
		LOG.debug("previous file deleted");
	    } catch (IOException e) {
		throw new BusinessException("couldn't delete the previous same flux file from working directory", e);
	    }
	} else {
	    hashCodeOfLog = getNullHash();
	    LOG.debug("null hash used");
	}
	LOG.debug("previous file hashCode computed");
	return hashCodeOfLog;
    }
}
