package ch.ge.cti.logchainer.exception.handler;

import static ch.qos.logback.classic.Level.OFF;
import static ch.qos.logback.classic.Level.WARN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.AfterClass;
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
	LOG = (Logger) LogChainerExceptionHandlerServiceImpl.log;
	LOG.setLevel(OFF);
    }

    @Test(description = "testing the way of processing an exception")
    public void handling_an_exception_should_comply_with_a_process() {
	ExceptionMessageLoaderImpl messageLoader = mock(ExceptionMessageLoaderImpl.class);
	handler.messageLoader = messageLoader;

	BusinessException businessException = new BusinessException("testing");
	CorruptedKeyException corruptedKeyException = new CorruptedKeyException("testing");
	NameException nameException = new NameException("testing");
	WatchServiceException watchServiceException = new WatchServiceException("testing", new Throwable());
	RuntimeException runTimeException = new RuntimeException("testing");

	when(messageLoader.getExceptionMessage(any())).thenReturn("{}");
	when(messageLoader.isProgrammToBeInterrupted()).thenReturn(true);

	// test the BusinessException interruption
	try {
	    handler.handleException(businessException);
	} catch (BusinessException e) {
	    assertEquals(e.getClass(), BusinessException.class, "businessException wasn't detected");
	}

	// test the CorruptedKeyException interruption
	try {
	    handler.handleException(corruptedKeyException);
	} catch (CorruptedKeyException e) {
	    assertEquals(e.getClass(), CorruptedKeyException.class, "CorruptedKeyException wasn't detected");
	}

	// test the NameException interruption
	try {
	    handler.handleException(nameException);
	} catch (NameException e) {
	    assertEquals(e.getClass(), NameException.class, "NameException wasn't detected");
	}

	// test the WatchServiceException interruption
	try {
	    handler.handleException(watchServiceException);
	} catch (WatchServiceException e) {
	    assertEquals(e.getClass(), WatchServiceException.class, "WatchServiceException wasn't detected");
	}

	// test the RuntimeException interruption
	try {
	    handler.handleException(runTimeException);
	} catch (RuntimeException e) {
	    assertEquals(e.getClass(), RuntimeException.class, "RuntimeException wasn't detected");
	}
    }

    @AfterClass
    public void tearDown() {
	// reset log Level to original one
	LOG.setLevel(WARN);
    }
}
