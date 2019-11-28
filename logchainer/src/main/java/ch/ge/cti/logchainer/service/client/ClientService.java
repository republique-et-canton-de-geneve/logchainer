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

package ch.ge.cti.logchainer.service.client;

import java.util.List;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;

public interface ClientService {
    /**
     * Register a file as a new WatchedFile object if it's not already existing
     * and adding it to the client's file list
     * 
     * @param client
     * @return the corrupted file or null if there are none
     */
    List<WatchedFile> registerEvent(Client client, boolean withHisto);

    /**
     * Register history files as new WatchedFile objects
     * 
     * @param client
     */
    void registerEventHistory(Client client);
    /**
     * Remove all flux that have been entirely processed from the client's map.
     * 
     * @param allDoneFlux
     * @param client
     */
    void removeAllProcessedFluxesFromMap(List<String> allDoneFlux, Client client);
}
