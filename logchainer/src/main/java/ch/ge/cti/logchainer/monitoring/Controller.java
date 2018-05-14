package ch.ge.cti.logchainer.monitoring;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@RestController
class Controller {
    static CompositeMeterRegistry composite;
    private Metrics metric;

    @RequestMapping("/actuator/global")
    public CompositeMeterRegistry globalInfos() {
	metric = new Metrics();
	composite = new CompositeMeterRegistry();

	metric.registerTotalNbCorruptedFiles();
	metric.registerAllCorruptedFiles();

	return composite;
    }

    @RequestMapping("/actuator/{client}")
    @ResponseBody
    public CompositeMeterRegistry clientInfos(@PathVariable String client) {
	metric = new Metrics();
	composite = new CompositeMeterRegistry();

	metric.registerAllCorruptedFiles(client);

	return composite;
    }
}
