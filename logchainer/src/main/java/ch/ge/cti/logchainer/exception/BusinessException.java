package ch.ge.cti.logchainer.exception;

/**
 * Exception de base utilisée pour traiter les erreurs métiers.
 */
@SuppressWarnings("serial")
public class BusinessException extends RuntimeException {
//    private static final long serialVersionUID = 6088765962073071589L;
    private final String locationError;

    /**
     * @param messageKey   clé du message résolvable par le bean messageSource
     * @param fieldInError nom de l'attribut en erreur
     * @param parameters   liste des paramètres associés au message
     */
    public BusinessException(String locationError) {
	super();
	this.locationError = locationError;
    }

    /**
     * @param messageKey   clé du message résolvable par le bean messageSource
     * @param fieldInError nom de l'attribut en erreur
     * @param cause        cause de l'erreur de validation si provoquée par une exception
     * @param parameters   liste des paramètres associés au message
     */
    public BusinessException(String locationError, Throwable cause) {
	super(cause);
	this.locationError = locationError;
    }
    
    public BusinessException(Throwable cause) {
	this("", cause);
    }

    public String getlocationError() {
	return locationError;
    }


}
