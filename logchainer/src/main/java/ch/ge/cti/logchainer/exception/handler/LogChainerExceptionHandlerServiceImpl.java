package ch.ge.cti.logchainer.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.exception.loader.ExceptionMessageLoaderImpl;

@Service
public class LogChainerExceptionHandlerServiceImpl implements LogChainerExceptionHandlerService {
    @Autowired
    ExceptionMessageLoaderImpl messageLoader;
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogChainerExceptionHandlerServiceImpl.class.getName());

    @Override
    public void handleException(RuntimeException exception) {
	if (exception instanceof BusinessException) {
	    BusinessException businessException = (BusinessException) exception;
	    String message = messageLoader.getExceptionMessage(businessException);
	    LOG.error(message, businessException.getArgError());

	    if (messageLoader.isProgrammToBeInterrupted())
		throw businessException;
	} else {
	    LOG.error("Unhandled runtime exception occurred", exception);
	    throw exception;
	}
    }
}
