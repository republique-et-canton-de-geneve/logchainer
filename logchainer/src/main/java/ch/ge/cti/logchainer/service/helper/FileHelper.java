package ch.ge.cti.logchainer.service.helper;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.constant.LogChainerConstant;

public class FileHelper {
    public String getSorter(Client client) {
	if (!client.getConf().getFilePattern().getSortingType().isEmpty()) {
	    return client.getConf().getFilePattern().getSortingType();
	} else {
	    return LogChainerConstant.SORT_DEFAULT;
	}
    }

    public String getSeparator(Client client) {
	if (!client.getConf().getFilePattern().getSeparator().isEmpty()) {
	    return client.getConf().getFilePattern().getSeparator();
	} else {
	    return LogChainerConstant.SEPARATOR_DEFAULT;
	}
    }

    public String getEncodingType(Client client) {
	if (!client.getConf().getFileEncoding().isEmpty()) {
	    return client.getConf().getFileEncoding();
	} else {
	    return LogChainerConstant.ENCODING_TYPE_DEFAULT;
	}
    }

    public String getStampPosition(Client client) {
	if (!client.getConf().getFilePattern().getStampPosition().isEmpty()) {
	    return client.getConf().getFilePattern().getStampPosition();
	} else {
	    return LogChainerConstant.STAMP_POSITION_DEFAULT;
	}
    }
}
