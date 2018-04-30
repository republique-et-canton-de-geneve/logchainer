package ch.ge.cti.logchainer.exception.loader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;

import javax.xml.bind.JAXBException;

import org.testng.annotations.Test;

import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.exception.CorruptedKeyException;
import ch.ge.cti.logchainer.exception.NameException;
import ch.ge.cti.logchainer.exception.WatchServiceException;

public class ExceptionMessageLoaderTest {
    private ExceptionMessageLoaderImpl messageLoader = new ExceptionMessageLoaderImpl();
    private final String corruptedKeyExceptionMessage = "corruptedKeyException";
    private final String fileAlreadyExistsExceptionMessage = "fileAlreadyExistsException";
    private final String fileNotFoundExceptionMessage = "fileNotFoundException";
    private final String ioExceptionMessage = "ioException";
    private final String jaxbExceptionMessage = "jaxbException";
    private final String nameExceptionMessage = "nameException";
    private final String unsupportedEncodingExceptionMessage = "unsupportedEncodingException";
    private final String watchServiceErrorMessage = "watchServiceError";

    @Test(description = "testing the obtention of error messages")
    public void testGetExceptionMessage() {
	messageLoader.corruptedKeyException = corruptedKeyExceptionMessage;
	messageLoader.fileAlreadyExistsException = fileAlreadyExistsExceptionMessage;
	messageLoader.fileNotFoundException = fileNotFoundExceptionMessage;
	messageLoader.ioException = ioExceptionMessage;
	messageLoader.jaxbException = jaxbExceptionMessage;
	messageLoader.nameException = nameExceptionMessage;
	messageLoader.unsupportedEncodingException = unsupportedEncodingExceptionMessage;
	messageLoader.watchServiceError = watchServiceErrorMessage;

	assertEquals(messageLoader.getExceptionMessage(new CorruptedKeyException("testing")),
		corruptedKeyExceptionMessage);
	assertFalse(messageLoader.isProgrammToBeInterrupted());

	assertEquals(
		messageLoader.getExceptionMessage(new BusinessException(new FileAlreadyExistsException("testing"))),
		fileAlreadyExistsExceptionMessage);
	assertTrue(messageLoader.isProgrammToBeInterrupted());

	assertEquals(messageLoader.getExceptionMessage(new BusinessException(new FileNotFoundException("testing"))),
		fileNotFoundExceptionMessage);
	assertTrue(messageLoader.isProgrammToBeInterrupted());
	
	assertEquals(messageLoader.getExceptionMessage(new BusinessException(new NoSuchFileException("testing"))),
		fileNotFoundExceptionMessage);
	assertTrue(messageLoader.isProgrammToBeInterrupted());

	assertEquals(messageLoader.getExceptionMessage(new BusinessException(new IOException("testing"))),
		ioExceptionMessage);
	assertTrue(messageLoader.isProgrammToBeInterrupted());

	assertEquals(messageLoader.getExceptionMessage(new BusinessException(new JAXBException("testing"))),
		jaxbExceptionMessage);
	assertTrue(messageLoader.isProgrammToBeInterrupted());

	assertEquals(messageLoader.getExceptionMessage(new NameException("testing")), nameExceptionMessage);
	assertFalse(messageLoader.isProgrammToBeInterrupted());

	assertEquals(
		messageLoader.getExceptionMessage(new BusinessException(new UnsupportedEncodingException("testing"))),
		unsupportedEncodingExceptionMessage);
	assertTrue(messageLoader.isProgrammToBeInterrupted());

	assertEquals(messageLoader.getExceptionMessage(new WatchServiceException("testing", new IOException())),
		watchServiceErrorMessage);
	assertTrue(messageLoader.isProgrammToBeInterrupted());
    }
}
