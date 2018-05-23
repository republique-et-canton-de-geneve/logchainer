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

package ch.ge.cti.logchainer.service.logchainer;

@FunctionalInterface
public interface LogChainerService {
    /**
     * Writes by inserting the byte array 'content' in the file 'filename' where
     * the byte insertion starts at the 'offset'.
     * 
     * @param filename
     *            - the name of the file to be written in.
     * @param offset
     *            - where the insertion starts (0 for the beginning)
     * @param content
     *            - message to insert, has to be converted as a byte array
     */
    void chainingLogFile(String filename, long offset, byte[] content);
}
