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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;
import ch.ge.cti.logchainer.constant.LogChainerConstant;
import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.service.flux.FluxService;
import ch.ge.cti.logchainer.service.helper.FileHelper;

@Service
public class ClientServiceImpl implements ClientService {
    FileHelper fileHelper = new FileHelper();

    @Autowired
    FluxService fluxService;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClientServiceImpl.class.getName());

    @Override
    @SuppressWarnings("unchecked")
    public List<WatchedFile> registerEvent(Client client, boolean withHisto) {
	List<WatchedFile> corruptedFiles = new ArrayList<>();

	// Management of the present files in the input folder before the logchainer execution
	if (withHisto) {
	    registerEventHistory(client);
	}
	// iterate on all events in the key
	for (WatchEvent<?> event : client.getKey().pollEvents()) {
	    WatchedFile fileToRegister = new WatchedFile(((WatchEvent<Path>) event).context().toString());
	    boolean toRegister = true;

	    LOG.debug("Processing event from file : {}", fileToRegister.getFilename());

	    if (LogChainerConstant.HISTORY_TRIGGER_FILENAME.equals(fileToRegister.getFilename())) {
		LOG.debug("History Event");
		continue;
	    }
	    
	    // check the validity of the filename
	    if (!fileToRegister.getFilename().contains(fileHelper.getSeparator(client))) {
		corruptedFiles.add(fileToRegister);
	    }

	    // check if the file has already been registered
	    for (WatchedFile file : client.getWatchedFiles()) {
		if (file.getFilename().equals(fileToRegister.getFilename())) {
		    toRegister = false;
		    LOG.debug("file already registered");
		}
	    }

	    // register the file and instantiate it
	    if (toRegister) {
		fileToRegister.setKind(event.kind());
		fileToRegister.setRegistered(false);
		client.getWatchedFiles().add(fileToRegister);
		LOG.debug("file registered");
	    }
	}

	return corruptedFiles.isEmpty() ? null : corruptedFiles;
    }

    @Override    
    public void registerEventHistory(Client client) {
	Path inputDirPath = Paths.get(client.getConf().getInputDir());
	// Manage History files
	try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDirPath)) {
	    WatchKey watchKey = inputDirPath.register(client.getWatcher(), ENTRY_CREATE);
	    client.setKey(watchKey);
	    for (Path entry : stream) {
		BasicFileAttributes attr = Files.readAttributes(entry, BasicFileAttributes.class);
		WatchedFile fileToRegister = new WatchedFile(entry.getFileName().toString(), attr.creationTime().toMillis());
		fileToRegister.setKind(ENTRY_CREATE);
		fileToRegister.setRegistered(false);
		client.getWatchedFiles().add(fileToRegister);
	    }
	} catch (IOException e) {
	    throw new BusinessException(e);
	}
    }

    @Override
    public void removeAllProcessedFluxesFromMap(List<String> allDoneFlux, Client client) {
	// remove the flux one by one
	for (String fluxname : allDoneFlux) {
	    client.getWatchedFiles().removeAll(client.getWatchedFilesByFlux().get(fluxname));
	    if (fluxService.removeFlux(fluxname, client)) {
		LOG.debug("flux {} has been removed from the map", fluxname);
	    } else {
		LOG.error("could not delete flux from map");
	    }
	}
    }
}
