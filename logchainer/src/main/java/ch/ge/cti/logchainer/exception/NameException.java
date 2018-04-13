package ch.ge.cti.logchainer.exception;

@SuppressWarnings("serial")
public class NameException extends BusinessException {
    public NameException(String message, Object... parameters) {
	super(message, parameters);
    }
    
}
