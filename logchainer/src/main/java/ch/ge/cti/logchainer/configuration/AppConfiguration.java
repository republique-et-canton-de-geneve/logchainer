package ch.ge.cti.logchainer.configuration;

import org.springframework.context.annotation.*;

@Configuration
@PropertySources({ @PropertySource(value = "file:${application.properties}"),
	@PropertySource(value = "file:${errorMessages.properties}") })
@ComponentScan("ch.ge.cti.logchainer")
public class AppConfiguration {
}
