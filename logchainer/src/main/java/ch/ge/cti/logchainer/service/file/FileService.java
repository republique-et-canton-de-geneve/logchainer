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

package ch.ge.cti.logchainer.service.file;

import java.util.List;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;

public interface FileService {
    /**
     * Register the file and relates it with it's client.
     * 
     * @param client
     * @param file
     */
    void registerFile(Client client, WatchedFile file);

    /**
     * Process of the file as an entry create.
     * 
     * @param clientNb
     * @param filename
     */
    void newFileProcess(Client client, String filename);

    /**
     * Sort the files of a specified flux using a specified sorting type.
     * 
     * @param separator
     * @param sorter
     * @param files
     */
    void sortFiles(String separator, String sorter, String stampPosition, List<WatchedFile> files);
}
