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

	metric.registerCorruptedFiles(client);

	return composite;
    }
}
