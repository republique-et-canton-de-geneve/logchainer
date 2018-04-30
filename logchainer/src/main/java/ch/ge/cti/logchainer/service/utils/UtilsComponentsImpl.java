package ch.ge.cti.logchainer.service.utils;

import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.constant.LogChainerConstant;

@Service
public class UtilsComponentsImpl implements UtilsComponents {
    @Override
    public String getSorter(Client client) {
	if (!client.getConf().getFilePattern().getSortingType().isEmpty()) {
	    return client.getConf().getFilePattern().getSortingType();
	} else {
	    return LogChainerConstant.SORT_DEFAULT;
	}
    }

    @Override
    public String getSeparator(Client client) {
	if (!client.getConf().getFilePattern().getSeparator().isEmpty()) {
	    return client.getConf().getFilePattern().getSeparator();
	} else {
	    return LogChainerConstant.SEPARATOR_DEFAULT;
	}
    }

    @Override
    public String getEncodingType(Client client) {
	if (!client.getConf().getFileEncoding().isEmpty()) {
	    return client.getConf().getFileEncoding();
	} else {
	    return LogChainerConstant.ENCODING_TYPE_DEFAULT;
	}
    }
}
