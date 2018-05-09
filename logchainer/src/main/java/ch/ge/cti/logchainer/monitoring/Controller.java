//package ch.ge.cti.logchainer.monitoring;
//
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import io.micrometer.core.instrument.Counter;
//import io.micrometer.core.instrument.Metrics;
//import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
//import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
//
//@RestController
//class Controller {
////    private CompositeMeterRegistry composite = new CompositeMeterRegistry();
//    
//    @RequestMapping("/actuator/ok")
//    public String debut() {
//	
////	Metrics metric = new Metrics(composite);
////	metric.saveNbCorruptedFilesTotal();
//	
////	CompositeMeterRegistry composite = new CompositeMeterRegistry();
//	SimpleMeterRegistry registry = new SimpleMeterRegistry();
////	composite.add(registry);
////	Counter counter = Counter.builder("corruptedFilesNb")
////		.description("counts the number of files that have been transfered to the corruptedFiles directory")
////		.tags("corrupted.files.detected.number", "arf")
////		.register(registry);
////	
////	counter.increment();
//	
//	return "54";
////	return counter;
//    }
//}
