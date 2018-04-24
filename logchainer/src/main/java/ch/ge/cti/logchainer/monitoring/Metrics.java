package ch.ge.cti.logchainer.monitoring;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.graphite.GraphiteMeterRegistry;;

public class Metrics {
    private final Counter counter;

    public Metrics(MeterRegistry registry) {
	this.counter = registry.counter("received.messages");
    }

    public void handleMessage(String message) {
	this.counter.increment();
	// handle message implementation
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
	return registry -> registry.config().commonTags("region", "us-east-1");
    }

    @Bean
    MeterRegistryCustomizer<GraphiteMeterRegistry> graphiteMetricsNamingConvention() {
	return registry -> registry.config().namingConvention(null);
    }

    //	private final List<String> words = new CopyOnWriteArrayList<>();
    //
    //	public Metrics(MeterRegistry registry) {
    //		registry.gaugeCollectionSize("dictionary.size", Tags.empty(), this.words);
    //	}

}
