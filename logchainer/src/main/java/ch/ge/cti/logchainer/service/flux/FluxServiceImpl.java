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

package ch.ge.cti.logchainer.service.flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;
import ch.ge.cti.logchainer.service.file.FileService;
import ch.ge.cti.logchainer.service.folder.FolderService;
import ch.ge.cti.logchainer.service.helper.FileHelper;
import ch.ge.cti.logchainer.service.logwatcher.LogWatcherService;

@Service
public class FluxServiceImpl implements FluxService {
    private final String stampPositionIsBefore = "before";
    FileHelper fileHelper = new FileHelper();

    @Autowired
    FileService fileService;
    @Autowired
    LogWatcherService watcherService;
    @Autowired
    FolderService mover;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FluxServiceImpl.class.getName());

    @Override
    public void addFlux(String fluxname, Client client) {
	LOG.debug("adding flux {} to client {}", fluxname, client.getConf().getClientId());
	client.getWatchedFilesByFlux().putIfAbsent(fluxname, new ArrayList<WatchedFile>());
    }

    @Override
    public boolean removeFlux(String fluxname, Client client) {
	LOG.debug("removing flux {} from list", fluxname);
	return client.getWatchedFilesByFlux().remove(fluxname) != null;
    }

    @Override
    public void addFileToFlux(String fluxname, WatchedFile file, Client client) {
	LOG.debug("mapping file {} to the flux {}", file.getFilename(), fluxname);
	client.getWatchedFilesByFlux().get(fluxname).add(file);
    }

    @Override
    public String getFluxName(String filename, String separator, String stampPosition) {
	LOG.debug("getting flux name method entered");
	StringBuilder fluxNameTmp = new StringBuilder();

	// find the flux name of the file
	String[] nameComponents = filename.split(separator);

	int start;
	int end;
	if (stampPosition.equals(stampPositionIsBefore)) {
	    start = 1;
	    end = nameComponents.length;
	} else {
	    start = 0;
	    end = nameComponents.length - 1;
	}

	for (int i = start; i < end; ++i) {
	    fluxNameTmp.append(nameComponents[i]);
	}
	if (LOG.isDebugEnabled())
	    LOG.debug("the flux of the file {} is : {}", filename, fluxNameTmp.toString());

	return stampPosition.equals(stampPositionIsBefore) ? fluxNameTmp.toString().split("\\.")[0]
		: fluxNameTmp.toString();
    }

    @Override
    public String getSortingStamp(String filename, String separator, String stampPosition) {
	LOG.debug("getting stamp method entered");
	String[] nameComponents = filename.split(separator);
	String[] nameStampComponents;

	if (stampPosition.equals(stampPositionIsBefore)) {
	    LOG.debug("the stamp of the file {} is : {}", filename, nameComponents[0]);
	    return nameComponents[0].trim();
	} else {
	    nameStampComponents = nameComponents[nameComponents.length - 1].split("\\.");
	    LOG.debug("the stamp of the file {} is : {}", filename, nameStampComponents[0]);
	    return nameStampComponents[0].trim();
	}
    }

    @Override
    public boolean isFluxReadyToBeProcessed(Map.Entry<String, ArrayList<WatchedFile>> flux) {
	boolean fluxReadyToBeProcessed = true;
	// check if all files in a flux are ready to be processed
	for (WatchedFile file : flux.getValue()) {
	    // check of the file
	    if (!file.isReadyToBeProcessed())
		fluxReadyToBeProcessed = false;
	}

	if (fluxReadyToBeProcessed)
	    LOG.debug("flux ready to be processed");

	return fluxReadyToBeProcessed;
    }

    @Override
    public void fluxProcess(Client client, List<String> allDoneFlux, Map.Entry<String, ArrayList<WatchedFile>> flux) {
	LOG.debug("flux {} starting to be processed", flux.getKey());
	fileService.sortFiles(fileHelper.getSeparator(client), fileHelper.getSorter(client),
		fileHelper.getStampPosition(client), flux.getValue());
	LOG.debug("flux sorted");

	// chek if all files' process has been completed
	boolean finished = true;
	// iterate on all the files of one flux
	for (WatchedFile file : flux.getValue()) {
	    String filename = file.getFilename();
	    // check if the file's process is complete
	    if (!watcherService.processAfterDetectionOfEvent(client, filename, file))
		finished = false;
	}
	// register the flux as completed (thus ready for deletion)
	if (finished) {
	    allDoneFlux.add(flux.getKey());
	    LOG.info("flux {} entirely processed", flux.getKey());
	}
    }

    @Override
    public void corruptedFluxProcess(Client client, List<String> allDoneFlux,
	    Map.Entry<String, ArrayList<WatchedFile>> flux) {
	// iterate on all the files of one flux
	for (WatchedFile file : flux.getValue()) {
	    String filename = file.getFilename();
	    // check if the file's process is complete
	    mover.moveFileInDirWithNoSameNameFile(filename, client.getConf().getInputDir(),
		    client.getConf().getCorruptedFilesDir());
	}
	// register the flux as completed (thus ready for deletion)
	allDoneFlux.add(flux.getKey());
	if (LOG.isInfoEnabled())
	    LOG.info("flux {} entirely processed", flux.getKey());
    }
}
