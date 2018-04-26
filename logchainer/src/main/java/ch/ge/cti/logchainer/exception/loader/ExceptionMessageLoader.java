package ch.ge.cti.logchainer.exception.loader;

import ch.ge.cti.logchainer.exception.BusinessException;

public interface ExceptionMessageLoader {
    /**
     * Get the correct error message related to the exception occurring.
     * 
     * @param e
     * @return the message
     */
    String getExceptionMessage(BusinessException e);

    /**
     * Define if the programm must be stopped because of the exception.
     * 
     * @return
     */
    boolean isProgrammToBeInterrupted();
}
