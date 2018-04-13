package ch.ge.cti.logchainer.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({ @PropertySource(value = "file:${application.properties}"),
	@PropertySource(value = "file:${errorMessages.properties}") })
@ComponentScan("ch.ge.cti.logchainer")
public class AppConfiguration {
}
