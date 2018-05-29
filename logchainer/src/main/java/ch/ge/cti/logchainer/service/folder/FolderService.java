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

package ch.ge.cti.logchainer.service.folder;

public interface FolderService {
    /**
     * Moves indicated file from the input Directory to the tmp one.
     * 
     * @param pathFile
     * @param pathProvidingDir
     * @param pathArrivingDir
     * @return file's path once moved
     */
    String moveFileInDirWithNoSameNameFile(String pathFile, String pathProvidingDir, String pathArrivingDir);

    /**
     * Moves indicated file from the tmp Directory to the output one.
     * 
     * @param pathFile
     * @param pathProvidingDir
     * @param pathArrivingDir
     * @return file's path once moved
     */
    String copyFileToDirByReplacingExisting(String pathFile, String pathProvidingDir, String pathArrivingDir);
}
