/*
 * <Log Chainer>
 *
 * Copyright (C) 2018 République et Canton de Genève
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
