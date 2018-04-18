package ch.ge.cti.logchainer.exception;

/**
 * Exception used in case of expected exceptions occurrence.
 */
@SuppressWarnings("serial")
public class BusinessException extends RuntimeException {
    private final String argError;

    /**
     * @param argError
     *            file or localization where the error occurred
     */
    public BusinessException(String argError) {
	super();
	this.argError = argError;
    }

    /**
     * @param argError
     *            file or localization where the error occurred
     * @param cause
     *            cause of the error
     */
    public BusinessException(String argError, Throwable cause) {
	super(cause);
	this.argError = argError;
    }

    /**
     * @param cause
     *            cause of the error
     */
    public BusinessException(Throwable cause) {
	this("", cause);
    }

    public String getArgError() {
	return argError;
    }

}
