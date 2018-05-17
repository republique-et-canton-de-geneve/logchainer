package ch.ge.cti.logchainer.exception;

/**
 * Class for the exception occurring on the WatchKey.
 */
@SuppressWarnings("serial")
public class CorruptedKeyException extends BusinessException {
    public CorruptedKeyException(String locationError) {
	super(locationError);
    }
}
