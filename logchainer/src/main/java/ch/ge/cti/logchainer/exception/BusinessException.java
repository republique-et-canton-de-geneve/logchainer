package ch.ge.cti.logchainer.exception;

/**
 * Exception de base utilisée pour traiter les erreurs métiers.
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 6088765962073071589L;

    private final String fieldInError;
    private final String messageKey;
    private final Object[] parameters;

    /**
     * @param messageKey   clé du message résolvable par le bean messageSource
     * @param fieldInError nom de l'attribut en erreur
     * @param parameters   liste des paramètres associés au message
     */
    public BusinessException(String messageKey, String fieldInError, Object... parameters) {
	super();
	this.messageKey = messageKey;
	this.fieldInError = fieldInError;
	this.parameters = parameters;
    }

    /**
     * @param messageKey   clé du message résolvable par le bean messageSource
     * @param fieldInError nom de l'attribut en erreur
     * @param cause        cause de l'erreur de validation si provoquée par une exception
     * @param parameters   liste des paramètres associés au message
     */
    public BusinessException(String messageKey, String fieldInError, Throwable cause, Object... parameters) {
	super(cause);
	this.messageKey = messageKey;
	this.fieldInError = fieldInError;
	this.parameters = parameters;
    }

    /**
     * @param messageKey clé du message résolvable par le bean messageSource
     */
    public BusinessException(String messageKey) {
	this(messageKey, "");
    }

    /**
     * @param messageKey clé du message résolvable par le bean messageSource
     * @param parameters liste des paramètres associés au message
     */
    public BusinessException(String messageKey, Object... parameters) {
	this(messageKey, "", parameters);
    }
    
    public BusinessException(Throwable cause) {
	this("", "", cause);
    }
    
    public BusinessException(String message, Throwable cause) {
	this(message, "", cause);
    }

    public String getFieldInError() {
	return fieldInError;
    }

    public String getMessageKey() {
	return messageKey;
    }

    public Object[] getParameters() {
	return parameters;
    }
}
