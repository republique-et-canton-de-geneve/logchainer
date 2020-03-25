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

package ch.ge.cti.logchainer.constant;

public final class LogChainerConstant {
    public static final String SEPARATOR_DEFAULT = "_";

    public static final String SORT_DEFAULT = "numerical";

    public static final String ENCODING_TYPE_DEFAULT = "UTF-8";

    public static final String STAMP_POSITION_DEFAULT = "after";
    public static final String MODE_BATCH = "batch";

    // in seconds
    public static final int DELAY_TRANSFER_FILE = 7;
    
    public static final String HISTORY_TRIGGER_FILENAME = "_garbageHistoryTrigger.tmp";    

    private LogChainerConstant() {
    }
}
