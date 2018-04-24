package ch.ge.cti.logchainer.service.utils;

import ch.ge.cti.logchainer.beans.Client;

public interface UtilsComponents {
    /**
     * Get the configured sorter String or the default one if none is defined by
     * the user. Default is "numerical".
     * 
     * @param client
     * @return the sorter
     */
    String getSorter(Client client);

    /**
     * Get the configured separator String or the default one if none is defined
     * by the user. Default is "_".
     * 
     * @param client
     * @return the separator
     */
    String getSeparator(Client client);

    /**
     * Get the configured encoding type String or the default one if none is
     * defined by the user. Default is "UTF-8".
     * 
     * @param client
     * @return the encoding type
     */
    String getEncodingType(Client client);
}
