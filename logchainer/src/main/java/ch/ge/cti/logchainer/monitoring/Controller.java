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

package ch.ge.cti.logchainer.monitoring;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

@RestController
class Controller {
    static CompositeMeterRegistry composite;
    private Metrics metric;

    /**
     * Informations about the programm.
     * 
     * @return registry containing the infos
     */
    @RequestMapping("/actuator/global")
    public CompositeMeterRegistry globalInfos() {
	metric = new Metrics();
	composite = new CompositeMeterRegistry();

	metric.registerTotalNbCorruptedFiles();
	metric.registerAllCorruptedFiles();

	return composite;
    }

    /**
     * Informations about the specified client.
     * 
     * @param client
     * @return registry containing the infos
     */
    @RequestMapping("/actuator/{client}")
    @ResponseBody
    public CompositeMeterRegistry clientInfos(@PathVariable String client) {
	metric = new Metrics();
	composite = new CompositeMeterRegistry();

	metric.registerCorruptedFiles(client);

	return composite;
    }
}
