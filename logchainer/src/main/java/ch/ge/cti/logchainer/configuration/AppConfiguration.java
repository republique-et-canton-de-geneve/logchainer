package ch.ge.cti.logchainer.configuration;

import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@PropertySource(value = "file:${application.properties}")
@ComponentScan("ch.ge.cti.logchainer")
public class AppConfiguration {
}
