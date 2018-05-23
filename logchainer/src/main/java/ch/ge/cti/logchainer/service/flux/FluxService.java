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

package ch.ge.cti.logchainer.service.flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;

public interface FluxService {
    /**
     * Add a flux to the flux list of the client.
     * 
     * @param fluxname
     * @param client
     */
    void addFlux(String fluxname, Client client);

    /**
     * Remove flux from the flux list of the client.
     * 
     * @param fluxname
     * @param client
     * @return
     */
    boolean removeFlux(String fluxname, Client client);

    /**
     * Add a file to a given flux in the map of the client.
     * 
     * @param fluxname
     * @param clientInfos
     * @param client
     */
    void addFileToFlux(String fluxname, WatchedFile clientInfos, Client client);

    /**
     * Get the flux name from a file.
     * 
     * @param filename
     * @return fluxname
     */
    String getFluxName(String filename, String separator, String stampPosition);

    /**
     * Get the stamp used to sort files.
     * 
     * @param filename
     * @param separator
     * @return stamp
     */
    String getSortingStamp(String filename, String separator, String stampPosition);

    /**
     * Check if the flux can be processed.
     * 
     * @param flux
     * @return
     */
    boolean isFluxReadyToBeProcessed(Map.Entry<String, ArrayList<WatchedFile>> flux);

    /**
     * Trigger the flux process.
     * 
     * @param client
     * @param allDoneFlux
     * @param flux
     */
    void fluxProcess(Client client, List<String> allDoneFlux, Map.Entry<String, ArrayList<WatchedFile>> flux);

    /**
     * Trigger a corrupted flux process.
     * 
     * @param client
     * @param allDoneFlux
     * @param flux
     */
    void corruptedFluxProcess(Client client, List<String> allDoneFlux, Map.Entry<String, ArrayList<WatchedFile>> flux);
}
