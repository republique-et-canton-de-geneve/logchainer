package ch.ge.cti.logchainer.exception;

/**
 * Exception used in case of expected exceptions occurrence.
 */
@SuppressWarnings("serial")
public class BusinessException extends RuntimeException {
    private final String locationError;

    /**
     * @param locationError
     *            file or localization where the error occurred
     */
    public BusinessException(String locationError) {
	super();
	this.locationError = locationError;
    }

    /**
     * @param locationError
     *            file or localization where the error occurred
     * @param cause
     *            cause of the error
     */
    public BusinessException(String locationError, Throwable cause) {
	super(cause);
	this.locationError = locationError;
    }

    /**
     * @param cause
     *            cause of the error
     */
    public BusinessException(Throwable cause) {
	this("", cause);
    }

    public String getlocationError() {
	return locationError;
    }

}
