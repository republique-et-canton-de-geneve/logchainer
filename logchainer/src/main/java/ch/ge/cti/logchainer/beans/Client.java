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

package ch.ge.cti.logchainer.beans;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ge.cti.logchainer.exception.WatchServiceException;
import ch.ge.cti.logchainer.generate.ClientConf;

/**
 * Bean grouping all client related attributs, such as the directories, the
 * watcher and it's watchKey.
 */
public class Client {
    private ClientConf conf;
    private WatchService watcher;
    private WatchKey key;
    private ArrayList<WatchedFile> watchedFiles;
    // Map the flux to the files it contains
    private Map<String, ArrayList<WatchedFile>> watchedFilesByFlux;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Client.class.getName());

    public Client(ClientConf conf) {
	LOG.debug("creating object Client");
	this.conf = conf;
	try {
	    this.watcher = FileSystems.getDefault().newWatchService();
	} catch (IOException e) {
	    throw new WatchServiceException(this.conf.getClientId(), e);
	}
	this.watchedFiles = new ArrayList<>();
	this.watchedFilesByFlux = new HashMap<>();
    }

    public WatchKey getKey() {
	return key;
    }

    public void setKey(WatchKey key) {
	LOG.debug("setting the key");
	this.key = key;
    }

    public WatchService getWatcher() {
	return watcher;
    }

    public ClientConf getConf() {
	return conf;
    }

    public List<WatchedFile> getWatchedFiles() {
	return watchedFiles;
    }

    public Map<String, ArrayList<WatchedFile>> getWatchedFilesByFlux() {
	return watchedFilesByFlux;
    }
}
