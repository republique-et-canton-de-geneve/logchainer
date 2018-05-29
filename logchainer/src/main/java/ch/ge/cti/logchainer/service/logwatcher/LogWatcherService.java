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

package ch.ge.cti.logchainer.service.logwatcher;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;
import ch.ge.cti.logchainer.generate.LogChainerConf;

public interface LogWatcherService {
    /**
     * Create a list where all clients are registered after being instantiated
     * as Client objects. Initialize their WatchKey.
     * 
     * @param clientConfList
     */
    void initializeFileWatcherByClient(LogChainerConf clientConfList);

    /**
     * Infinity loop checking for updates in the directoy and then does the file
     * process by calling all necessary methods correctly.
     */
    void processEvents();

    /**
     * Control in which way the file will be handled.
     * 
     * @param clientNb
     */
    boolean processAfterDetectionOfEvent(Client client, String filename, WatchedFile file);
}
