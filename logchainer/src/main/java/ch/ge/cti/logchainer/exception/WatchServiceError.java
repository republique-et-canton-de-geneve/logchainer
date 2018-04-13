package ch.ge.cti.logchainer.exception;

@SuppressWarnings("serial")
public class WatchServiceError extends BusinessException {
    public WatchServiceError(String locationError, Throwable e) {
	super(locationError, e);
    }
}
