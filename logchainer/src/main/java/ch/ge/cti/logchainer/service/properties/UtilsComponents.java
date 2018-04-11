package ch.ge.cti.logchainer.service.properties;

import ch.ge.cti.logchainer.beans.Client;

public interface UtilsComponents {
    
    String getSorter(Client client);
    
    String getSeparator(Client client);
    
    String getEncodingType(Client client);
}
