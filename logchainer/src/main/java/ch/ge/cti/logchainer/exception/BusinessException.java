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

package ch.ge.cti.logchainer.exception;

/**
 * Exception used in case of expected exceptions occurrence.
 */
@SuppressWarnings("serial")
public class BusinessException extends RuntimeException {
    private final String argError;

    /**
     * @param argError
     *            file or localization where the error occurred
     */
    public BusinessException(String argError) {
	super();
	this.argError = argError;
    }

    /**
     * @param argError
     *            file or localization where the error occurred
     * @param cause
     *            cause of the error
     */
    public BusinessException(String argError, Throwable cause) {
	super(cause);
	this.argError = argError;
    }

    /**
     * @param cause
     *            cause of the error
     */
    public BusinessException(Throwable cause) {
	this("", cause);
    }

    public String getArgError() {
	return argError;
    }

}
