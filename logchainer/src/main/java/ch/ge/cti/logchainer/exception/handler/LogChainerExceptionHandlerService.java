package ch.ge.cti.logchainer.exception.handler;

public interface LogChainerExceptionHandlerService {
    /**
     * Treat the exception regarding it's type.
     * 
     * @param exception
     */
    void handleException(RuntimeException exception);
}
