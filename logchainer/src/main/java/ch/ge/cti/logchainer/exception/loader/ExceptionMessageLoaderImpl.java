/*
 * <Log Chainer>
 *
 * Copyright (C) <Date, 2018> République et Canton de Genève
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

package ch.ge.cti.logchainer.exception.loader;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.exception.CorruptedKeyException;
import ch.ge.cti.logchainer.exception.NameException;
import ch.ge.cti.logchainer.exception.WatchServiceException;

@Service
public class ExceptionMessageLoaderImpl implements ExceptionMessageLoader {
    private boolean programmToBeInterrupted;

    @Value("${JAXBException}")
    String jaxbException;

    @Value("${FileNotFoundException}")
    String fileNotFoundException;

    @Value("${FileAlreadyExistsException}")
    String fileAlreadyExistsException;

    @Value("${UnsupportedEncodingException}")
    String unsupportedEncodingException;

    @Value("${NameException}")
    String nameException;

    @Value("${CorruptedKeyException}")
    String corruptedKeyException;

    @Value("${WatchServiceError}")
    String watchServiceError;

    @Value("${IOException}")
    String ioException;

    @Override
    public String getExceptionMessage(BusinessException e) {
	if (e.getCause() instanceof JAXBException) {
	    programmToBeInterrupted = true;
	    return jaxbException;
	} else if (e.getCause() instanceof FileNotFoundException || e.getCause() instanceof NoSuchFileException) {
	    programmToBeInterrupted = true;
	    return fileNotFoundException;
	} else if (e.getCause() instanceof FileAlreadyExistsException) {
	    programmToBeInterrupted = true;
	    return fileAlreadyExistsException;
	} else if (e.getCause() instanceof UnsupportedEncodingException) {
	    programmToBeInterrupted = true;
	    return unsupportedEncodingException;
	} else if (e instanceof NameException) {
	    programmToBeInterrupted = false;
	    return nameException;
	} else if (e instanceof CorruptedKeyException) {
	    programmToBeInterrupted = true;
	    return corruptedKeyException;
	} else if (e instanceof WatchServiceException) {
	    programmToBeInterrupted = true;
	    return watchServiceError;
	} else {
	    programmToBeInterrupted = true;
	    return ioException;
	}
    }

    @Override
    public boolean isProgrammToBeInterrupted() {
	return programmToBeInterrupted;
    }
}
