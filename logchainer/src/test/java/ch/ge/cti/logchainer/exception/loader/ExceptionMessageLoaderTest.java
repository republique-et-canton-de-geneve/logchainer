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
    public void the_exception_message_should_comply_with_a_pattern() {
	messageLoader.corruptedKeyException = corruptedKeyExceptionMessage;
	messageLoader.fileAlreadyExistsException = fileAlreadyExistsExceptionMessage;
	messageLoader.fileNotFoundException = fileNotFoundExceptionMessage;
	messageLoader.ioException = ioExceptionMessage;
	messageLoader.jaxbException = jaxbExceptionMessage;
	messageLoader.nameException = nameExceptionMessage;
	messageLoader.unsupportedEncodingException = unsupportedEncodingExceptionMessage;
	messageLoader.watchServiceError = watchServiceErrorMessage;

	assertEquals(messageLoader.getExceptionMessage(new CorruptedKeyException("testing")),
		corruptedKeyExceptionMessage,
		"exception message wasn't correctly transmitted for CorruptedKeyException");
	assertTrue(messageLoader.isProgrammToBeInterrupted(), "interruption isn't detected for CorruptedKeyException");

	assertEquals(
		messageLoader.getExceptionMessage(new BusinessException(new FileAlreadyExistsException("testing"))),
		fileAlreadyExistsExceptionMessage,
		"exception message wasn't correctly transmitted for FileAlreadyExistsException");
	assertTrue(messageLoader.isProgrammToBeInterrupted(),
		"interruption isn't detected for FileAlreadyExistsException");

	assertEquals(messageLoader.getExceptionMessage(new BusinessException(new FileNotFoundException("testing"))),
		fileNotFoundExceptionMessage,
		"exception message wasn't correctly transmitted for FileNotFoundException");
	assertTrue(messageLoader.isProgrammToBeInterrupted(), "interruption isn't detected for FileNotFoundException");

	assertEquals(messageLoader.getExceptionMessage(new BusinessException(new NoSuchFileException("testing"))),
		fileNotFoundExceptionMessage, "exception message wasn't correctly transmitted for NoSuchFileException");
	assertTrue(messageLoader.isProgrammToBeInterrupted(), "interruption isn't detected for NoSuchFileException");

	assertEquals(messageLoader.getExceptionMessage(new BusinessException(new IOException("testing"))),
		ioExceptionMessage, "exception message wasn't correctly transmitted for IOException");
	assertTrue(messageLoader.isProgrammToBeInterrupted(), "interruption isn't detected for IOException");

	assertEquals(messageLoader.getExceptionMessage(new BusinessException(new JAXBException("testing"))),
		jaxbExceptionMessage, "exception message wasn't correctly transmitted for JAXBException");
	assertTrue(messageLoader.isProgrammToBeInterrupted(), "interruption isn't detected for JAXBException");

	assertEquals(messageLoader.getExceptionMessage(new NameException("testing")), nameExceptionMessage,
		"exception message wasn't correctly transmitted for NameException");
	assertFalse(messageLoader.isProgrammToBeInterrupted(), "interruption is detected for NameException");

	assertEquals(
		messageLoader.getExceptionMessage(new BusinessException(new UnsupportedEncodingException("testing"))),
		unsupportedEncodingExceptionMessage,
		"exception message wasn't correctly transmitted for UnsupportedEncodingException");
	assertTrue(messageLoader.isProgrammToBeInterrupted(),
		"interruption isn't detected for UnsupportedEncodingException");

	assertEquals(messageLoader.getExceptionMessage(new WatchServiceException("testing", new IOException())),
		watchServiceErrorMessage, "exception message wasn't correctly transmitted for WatchServiceException");
	assertTrue(messageLoader.isProgrammToBeInterrupted(), "interruption isn't detected for WatchServiceException");
    }
}
