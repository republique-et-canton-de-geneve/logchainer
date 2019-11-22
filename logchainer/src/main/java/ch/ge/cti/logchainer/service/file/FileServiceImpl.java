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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;
import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.service.flux.FluxService;
import ch.ge.cti.logchainer.service.folder.FolderService;
import ch.ge.cti.logchainer.service.hash.HashService;
import ch.ge.cti.logchainer.service.helper.FileHelper;
import ch.ge.cti.logchainer.service.logchainer.LogChainerService;

@Service
public class FileServiceImpl implements FileService {
    FileHelper fileHelper = new FileHelper();

    @Autowired
    FolderService mover;
    @Autowired
    LogChainerService chainer;
    @Autowired
    HashService hasher;
    @Autowired
    FluxService fluxService;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FileServiceImpl.class.getName());

    @Override
    public void registerFile(Client client, WatchedFile file) {
	LOG.debug("registering file {}", file.getFilename());
	// get the name of the file's flux
	String fluxname = fluxService.getFluxName(file.getFilename(), fileHelper.getSeparator(client),
		fileHelper.getStampPosition(client));

	// register the flux if it doesn't already exist
	fluxService.addFlux(fluxname, client);
	LOG.debug("new flux {} added to the map", fluxname);

	// register the file as a relation to it's flux
	fluxService.addFileToFlux(fluxname, file, client);
	// indicate the file has been registered
	file.setRegistered(true);
	LOG.debug("file {} registered", file.getFilename());
    }

    @Override
    public void newFileProcess(Client client, String filename) {
	LOG.debug("New file detected : {}", (new File(client.getConf().getInputDir(), filename)).getAbsolutePath());

	// access same flux file in the tmp directory
	Collection<File> previousFiles = getPreviousFiles(
		fluxService.getFluxName(filename, fileHelper.getSeparator(client), fileHelper.getStampPosition(client)),
		client.getConf().getWorkingDir(), fileHelper.getSeparator(client), fileHelper.getStampPosition(client));

	// move the file to the tmp directory
	String pFileInTmp = mover.moveFileInDirWithNoSameNameFile(filename, client.getConf().getInputDir(),
		client.getConf().getWorkingDir());

	// chain the log of the previous file to the current one (with infos:
	// previous file name and date of chaining)
	try {
	    String message = messageToInsert(hasher.getPreviousFileHash(previousFiles), previousFiles, client);
	    chainer.chainingLogFile(pFileInTmp, 0, message.getBytes(fileHelper.getEncodingType(client)));
	} catch (UnsupportedEncodingException e) {
	    throw new BusinessException(e);
	}

	// release the file processed into the output directory to be processed
	// by
	// the user
	mover.copyFileToDirByReplacingExisting(filename, client.getConf().getWorkingDir(),
		client.getConf().getOutputDir());

	if (LOG.isInfoEnabled())
	    LOG.info("end of the process of the file {} put in the input directory", filename);
    }

    @Override
    public void sortFiles(String separator, String sorter, String stampPosition, List<WatchedFile> files) {
	LOG.debug("sorting the file list");
	if (("alphabetical").equals(sorter)) {
	    LOG.debug("sorting by alphabetical order");
	} else {
	    LOG.debug("sorting by numerical order");
	}
	// sorting algorithm
	Collections.sort(files, new Comparator<WatchedFile>() {
	    @Override
	    public int compare(WatchedFile file1, WatchedFile file2) {
		// get both file's stamp which are used to sort them
		String sortingStamp1 = fluxService.getSortingStamp(file1.getFilename(), separator, stampPosition);
		String sortingStamp2 = fluxService.getSortingStamp(file2.getFilename(), separator, stampPosition);

		// case where the sorting type is alphabetical
		if (("alphabetical").equals(sorter)) {
		    return sortingStamp1.compareTo(sortingStamp2);
		} else {
		    // case where the sorting type is numerical (default one)
		    Integer stamp1 = Integer.parseInt(sortingStamp1);
		    Integer stamp2 = Integer.parseInt(sortingStamp2);

		    return stamp1.compareTo(stamp2);
		}
	    }
	});
    }

    /**
     * Get the collection of all already existing same flux files in tmp
     * directory. (Should be only one)
     * 
     * @param fluxName
     * @return collection of these files
     */
    @SuppressWarnings("unchecked")
    private Collection<File> getPreviousFiles(String fluxName, String workingDir, String separator,
	    String stampPosition) {
	// filter the files to only keep the same as given flux one (should
	// be unique)
	return FileUtils.listFiles(new File(workingDir), new IOFileFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return accept(new File(name));
	    }

	    @Override
	    public boolean accept(File file) {
		if (fluxName.equals(fluxService.getFluxName(file.getName(), separator, stampPosition))) {
		    LOG.debug("same flux name noticed for {}", file.getName());
		} else {
		    LOG.debug("no same flux name detected");
		}
		return (fluxName.equals(fluxService.getFluxName(file.getName(), separator, stampPosition)) ? true
			: false);
	    }
	}, null);
    }

    /**
     * Create the text message to insert in the new file.
     * 
     * @param hashCodeOfLog
     * @param previousFiles
     * @return the message
     */
    String messageToInsert(byte[] hashCodeOfLog, Collection<File> previousFiles, Client client) {
	LOG.debug("computing the message to insert");
	// Chaining date
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+01:00'");
	// dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+1"));
	String date = dateFormat.format(new Date());

	// Name of the previous file
	String previousFile = " ";
	Optional<File> previousFirstFile = previousFiles.stream().findFirst();
	if (previousFirstFile.isPresent()) {
	    previousFile += previousFirstFile.get().getName() + "\n";
	} else {
	    previousFile += "\n";
	}

	// HashCode of the previous file
	String previousFileHashCode;
	try {
	    StringBuffer sbHashCode = new StringBuffer();
	    for (int i = 0; i < hashCodeOfLog.length; i++) {
		sbHashCode.append(Integer.toString((hashCodeOfLog[i] & 0xff) + 0x100, 16).substring(1));
	    }

	    String hash = new String(sbHashCode.toString().getBytes(), fileHelper.getEncodingType(client));
	    if (!StringUtils.isEmpty(hash)) {
			hash = hash.toUpperCase();
	    }
	    previousFileHashCode = "SHA-256:" + hash + "\n";
	} catch (UnsupportedEncodingException e) {
	    throw new BusinessException(e);
	}

	LOG.debug("message ready to be inserted");
	return date + previousFile + previousFileHashCode;
    }
}
