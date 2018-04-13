package ch.ge.cti.logchainer.exception;

public interface LogChainerExceptionHandlerService {
    /**
     * Treat the exception regarding it's type.
     * 
     * @param exception
     */
    void handleException(RuntimeException exception);
}
