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

	// finding the flux name of the file, knowing each flux is situated at
	// the beginning of the filename (before the separator)
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
	    return nameComponents[0];
	} else {
	    nameStampComponents = nameComponents[nameComponents.length - 1].split("\\.");
	    LOG.debug("the stamp of the file {} is : {}", filename, nameStampComponents[0]);
	    return nameStampComponents[0];
	}
    }

    @Override
    public boolean isFluxReadyToBeTreated(Map.Entry<String, ArrayList<WatchedFile>> flux) {
	boolean fluxReadyToBeTreated = true;
	// checking if all files in a flux are ready to be treated
	for (WatchedFile file : flux.getValue()) {
	    // check of the file
	    if (!file.isReadyToBeProcessed())
		fluxReadyToBeTreated = false;
	}

	if (fluxReadyToBeTreated)
	    LOG.debug("flux ready to be treated");

	return fluxReadyToBeTreated;
    }

    @Override
    public void fluxTreatment(Client client, List<String> allDoneFlux, Map.Entry<String, ArrayList<WatchedFile>> flux) {
	LOG.debug("flux {} starting to be treated", flux.getKey());
	fileService.sortFiles(fileHelper.getSeparator(client), fileHelper.getSorter(client),
		fileHelper.getStampPosition(client), flux.getValue());
	LOG.debug("flux sorted");

	// cheking if all files' treatment has been completed
	boolean finished = true;
	// iterating on all the files of one flux
	for (WatchedFile file : flux.getValue()) {
	    String filename = file.getFilename();
	    // checking if the file's treatment is complete
	    if (!watcherService.treatmentAfterDetectionOfEvent(client, filename, file))
		finished = false;
	}
	// registering the flux as completed (thus ready for deletion)
	if (finished) {
	    allDoneFlux.add(flux.getKey());
	    LOG.info("flux {} entirely treated", flux.getKey());
	}
    }

    @Override
    public void corruptedFluxProcess(Client client, List<String> allDoneFlux,
	    Map.Entry<String, ArrayList<WatchedFile>> flux) {
	// iterating on all the files of one flux
	for (WatchedFile file : flux.getValue()) {
	    String filename = file.getFilename();
	    // checking if the file's treatment is complete
	    mover.moveFileInDirWithNoSameNameFile(filename, client.getConf().getInputDir(),
		    client.getConf().getCorruptedFilesDir());
	}
	// registering the flux as completed (thus ready for deletion)
	allDoneFlux.add(flux.getKey());
	if (LOG.isInfoEnabled())
	    LOG.info("flux {} entirely treated", flux.getKey());
    }
}
