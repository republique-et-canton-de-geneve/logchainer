//package ch.ge.cti.logchainer.monitoring;
//
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//
////import io.micrometer.core.instrument.config.MeterFilter;
//
//public class Metrics {
////    private final Counter counter;
////
////    public Metrics(MeterRegistry registry) {
////	this.counter = registry.counter("received.messages");
////    }
////    
////    @Bean
////    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
////    	return registry -> registry.config().commonTags("region", "us-east-1");
////    }
////    
////    @Bean
////    MeterRegistryCustomizer<GraphiteMeterRegistry> graphiteMetricsNamingConvention() {
////    	return registry -> registry.config().namingConvention(MY_CUSTOM_CONVENTION);
////    }
//    
//	private final List<String> words = new CopyOnWriteArrayList<>();
//
//	Metrics(MeterRegistry registry) {
//		registry.gaugeCollectionSize("dictionary.size", Tags.empty(), this.words);
//	}
//
//}
