package ch.ge.cti.logchainer.exception;

@SuppressWarnings("serial")
public class NameException extends BusinessException {
    public NameException(String locationError) {
	super(locationError);
    }
}
