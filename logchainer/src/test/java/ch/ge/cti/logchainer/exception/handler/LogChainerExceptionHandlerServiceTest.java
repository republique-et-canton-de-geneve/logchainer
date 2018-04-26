package ch.ge.cti.logchainer.exception.handler;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.testng.Assert.*;

import org.testng.annotations.Test;

import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.exception.loader.ExceptionMessageLoaderImpl;

public class LogChainerExceptionHandlerServiceTest {
    private final LogChainerExceptionHandlerServiceImpl handler = new LogChainerExceptionHandlerServiceImpl();
    
    @Test(description = "testing the way of processing an exception")
    public void testHandleException() {
	ExceptionMessageLoaderImpl messageLoader = mock(ExceptionMessageLoaderImpl.class);
	handler.messageLoader = messageLoader;
	
//	handler.handleException(new BusinessException(cause));
    }
}
