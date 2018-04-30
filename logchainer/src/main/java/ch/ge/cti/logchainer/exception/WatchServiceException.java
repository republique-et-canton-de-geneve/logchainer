package ch.ge.cti.logchainer.exception;

@SuppressWarnings("serial")
public class WatchServiceException extends BusinessException {
    public WatchServiceException(String locationError, Throwable e) {
	super(locationError, e);
    }
}
