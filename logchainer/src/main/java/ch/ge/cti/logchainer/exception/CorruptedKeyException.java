package ch.ge.cti.logchainer.exception;

@SuppressWarnings("serial")
public class CorruptedKeyException extends BusinessException {
    public CorruptedKeyException(String locationError) {
	super(locationError);
    }
}
