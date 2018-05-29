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

package ch.ge.cti.logchainer.exception.loader;

import ch.ge.cti.logchainer.exception.BusinessException;

public interface ExceptionMessageLoader {
    /**
     * Get the correct error message related to the exception occurring.
     * 
     * @param e
     * @return the message
     */
    String getExceptionMessage(BusinessException e);

    /**
     * Define if the programm must be stopped because of the exception.
     * 
     * @return
     */
    boolean isProgrammToBeInterrupted();
}
