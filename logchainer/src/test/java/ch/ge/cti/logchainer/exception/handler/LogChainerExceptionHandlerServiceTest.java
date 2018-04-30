package ch.ge.cti.logchainer.exception.handler;

import static ch.qos.logback.classic.Level.OFF;
import static ch.qos.logback.classic.Level.WARN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.exception.CorruptedKeyException;
import ch.ge.cti.logchainer.exception.NameException;
import ch.ge.cti.logchainer.exception.WatchServiceException;
import ch.ge.cti.logchainer.exception.loader.ExceptionMessageLoaderImpl;
import ch.qos.logback.classic.Logger;

public class LogChainerExceptionHandlerServiceTest {
    private final LogChainerExceptionHandlerServiceImpl handler = new LogChainerExceptionHandlerServiceImpl();
    private Logger LOG;

    @BeforeTest
    public void setUp() {
	// Temporary mute of the logs not to get any logs of error while testing
	// the exceptions method
	LOG = (Logger) LogChainerExceptionHandlerServiceImpl.LOG;
	LOG.setLevel(OFF);
    }

    @Test(description = "testing the way of processing an exception")
    public void testHandleException() {
	ExceptionMessageLoaderImpl messageLoader = mock(ExceptionMessageLoaderImpl.class);
	handler.messageLoader = messageLoader;

	BusinessException businessException = new BusinessException("testing");
	CorruptedKeyException corruptedKeyException = new CorruptedKeyException("testing");
	NameException nameException = new NameException("testing");
	WatchServiceException watchServiceException = new WatchServiceException("testing", new Throwable());
	RuntimeException runTimeException = new RuntimeException("testing");

	when(messageLoader.getExceptionMessage(any())).thenReturn("{}");
	when(messageLoader.isProgrammToBeInterrupted()).thenReturn(true);

	// testing the BusinessException interruption
	try {
	    handler.handleException(businessException);
	} catch (BusinessException e) {
	    assertEquals(e.getClass(), BusinessException.class);
	}

	// testing the CorruptedKeyException interruption
	try {
	    handler.handleException(corruptedKeyException);
	} catch (CorruptedKeyException e) {
	    assertEquals(e.getClass(), CorruptedKeyException.class);
	}

	// testing the NameException interruption
	try {
	    handler.handleException(nameException);
	} catch (NameException e) {
	    assertEquals(e.getClass(), NameException.class);
	}

	// testing the WatchServiceException interruption
	try {
	    handler.handleException(watchServiceException);
	} catch (WatchServiceException e) {
	    assertEquals(e.getClass(), WatchServiceException.class);
	}

	// testing the RuntimeException interruption
	try {
	    handler.handleException(runTimeException);
	} catch (RuntimeException e) {
	    assertEquals(e.getClass(), RuntimeException.class);
	}
    }

    @AfterTest
    public void tearDown() {
	// reseting log Level to original one
	LOG.setLevel(WARN);
    }
}
