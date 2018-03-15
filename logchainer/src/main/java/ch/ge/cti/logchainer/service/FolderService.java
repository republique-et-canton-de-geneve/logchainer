package ch.ge.cti.logchainer.service;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ge.cti.logchainer.configuration.AppConfiguration;

public class FolderService {

	/**
	 * logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(FolderService.class.getName());

	private static String TMP_DIRECTORY = "tmpDirectory";

	/**
	 * Moves the newly created files (those who normally don't override any file
	 * name in the tmp directory)
	 * 
	 * @param pFile
	 * @throws IOException
	 */
	public static String moveFileInputToTmp(String pFile, String pDir) throws IOException {
		LOG.debug("new file moving method entered");

		Files.move(Paths.get(pDir + "/" + pFile), Paths.get(getTmpProperty(TMP_DIRECTORY) + "/" + pFile), new CopyOption[] {});

		LOG.debug("file successfully moved to directory : " + getTmpProperty(TMP_DIRECTORY) + "/" + pFile);

		return getTmpProperty(TMP_DIRECTORY) + "/" + pFile;
	}

	/**
	 * Getter for the tmp property.
	 * 
	 * @return tmp directory name as a String
	 */
	private static String getTmpProperty(String key) {
		String tmp;

		try {
			tmp = AppConfiguration.load().getProperty(key);

			LOG.info("tmp property correctly accessed");

			return tmp;

		} catch (Exception e) {
			LOG.error("tmp property not found");

			return null;
		}
	}

}
