package ch.ge.cti.logchainer.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogChainerExceptionHandlerService {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogChainerExceptionHandlerService.class.getName());

    public LogChainerExceptionHandlerService() {
    }

    public void handleException(RuntimeException exception) {
	if (exception.getClass().equals(BusinessException.class)) {
	    BusinessException businessException = (BusinessException) exception;
	    if (businessException.getClass().equals(NameException.class)) {
		LOG.error(businessException.getMessage(), businessException.getParameters()[0]);
	    } else {
		LOG.error(businessException.getMessage(), businessException.getParameters()[0],
			businessException.getCause());
		throw businessException;
	    }
	} else {
	    LOG.error("Unhandled runtime exception occurred", exception);
	    throw exception;
	}
    }
}
