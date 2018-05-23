/*
 * <Log Chainer>
 *
 * Copyright (C) <Date, 2018> République et Canton de Genève
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

package ch.ge.cti.logchainer.service.hash;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

public interface HashService {
    /**
     * Computes the SHA256 HashCode of the given stream.
     * 
     * @param fileStream
     * @return the result of the hash algorithm
     */
    byte[] getLogHashCode(InputStream fileStream);

    /**
     * To get a HashCode for a null object (defined by programmer)
     * 
     * @return empty byte array (arbitrary choice that can be changed)
     */
    byte[] getNullHash();

    /**
     * Get the hash of the tmp directory's already existing file.
     * 
     * @param previousFiles
     * @return previous file's hashCode
     */
    byte[] getPreviousFileHash(Collection<File> previousFiles);
}
