package ch.ge.cti.logchainer.exception;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExceptionMessageLoaderImpl implements ExceptionMessageLoader {
    private boolean programmToBeInterrupted;

    @Value("${JAXBException}")
    private String jaxbException;

    @Value("${FileNotFoundException}")
    private String fileNotFoundException;

    @Value("${FileAlreadyExistsException}")
    private String fileAlreadyExistsException;

    @Value("${NameException}")
    private String nameException;

    @Value("${CorruptedKeyException}")
    private String corruptedKeyException;

    @Value("${WatchServiceError}")
    private String watchServiceError;

    @Value("${IOException}")
    private String ioException;

    @Override
    public String getExceptionMessage(BusinessException e) {
	if (e.getCause() instanceof JAXBException) {
	    programmToBeInterrupted = true;
	    return jaxbException;
	} else if (e.getCause() instanceof FileNotFoundException) {
	    programmToBeInterrupted = true;
	    return fileNotFoundException;
	} else if (e.getCause() instanceof FileAlreadyExistsException) {
	    programmToBeInterrupted = true;
	    return fileAlreadyExistsException;
	} else if (e instanceof NameException) {
	    programmToBeInterrupted = false;
	    return nameException;
	} else if (e instanceof CorruptedKeyException) {
	    programmToBeInterrupted = false;
	    return corruptedKeyException;
	} else if (e instanceof WatchServiceError) {
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
