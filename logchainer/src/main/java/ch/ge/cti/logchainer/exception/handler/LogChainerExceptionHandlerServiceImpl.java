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
    static Logger log = LoggerFactory.getLogger(LogChainerExceptionHandlerServiceImpl.class.getName());

    @Override
    public void handleException(RuntimeException exception) {
	if (exception instanceof BusinessException) {
	    BusinessException businessException = (BusinessException) exception;
	    String message = messageLoader.getExceptionMessage(businessException);
	    log.error(message, businessException.getArgError());

	    if (messageLoader.isProgrammToBeInterrupted())
		throw businessException;
	} else {
	    log.error("Unhandled runtime exception occurred", exception);
	    throw exception;
	}
    }
}
