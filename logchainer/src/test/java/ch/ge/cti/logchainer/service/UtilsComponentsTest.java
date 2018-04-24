package ch.ge.cti.logchainer.service;

import static org.testng.Assert.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.configuration.TestConfiguration;
import ch.ge.cti.logchainer.constante.LogChainerConstante;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.FilePattern;
import ch.ge.cti.logchainer.service.properties.UtilsComponents;

@ContextConfiguration(classes = TestConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class UtilsComponentsTest extends AbstractTestNGSpringContextTests {
    private static final String customSeparator = "-";
    private static final String customSortingType = "alphabetical";
    private static final String customFileEncoding = "ISO-8859-1";

    private Client clientCustom;
    private Client clientDefault;
    
    @Autowired
    private UtilsComponents component;

    @BeforeTest
    public void prepClients() {
	ClientConf clientConfCustom = new ClientConf();
	FilePattern filePatternCustom = new FilePattern();

	filePatternCustom.setSeparator(customSeparator);
	filePatternCustom.setSortingType(customSortingType);

	clientConfCustom.setFilePattern(filePatternCustom);
	clientConfCustom.setClientId("ClientCustom");
	clientConfCustom.setFileEncoding(customFileEncoding);

	clientCustom = new Client(clientConfCustom);

	ClientConf clientConfDefault = new ClientConf();
	FilePattern filePatternDefault = new FilePattern();

	filePatternDefault.setSeparator("");
	filePatternDefault.setSortingType("");

	clientConfDefault.setFilePattern(filePatternDefault);
	clientConfDefault.setClientId("ClientDefault");
	clientConfDefault.setFileEncoding("");

	clientDefault = new Client(clientConfDefault);
    }

    @Test
    public void getSorterTest() {
	// checking that the correct value is returned for a custom component
	// part
	assertEquals(component.getSorter(clientCustom), customSortingType);

	// checking that if we don't have any value for a component part, we
	// still get the correct default value
	assertEquals(component.getSorter(clientDefault), LogChainerConstante.SORT_DEFAULT);
    }

    @Test
    public void getSeparatorTest() {
	// checking that the correct value is returned for a custom component
	// part
	assertEquals(component.getSeparator(clientCustom), customSeparator);

	// checking that if we don't have any value for a component part, we
	// still get the correct default value
	assertEquals(component.getSeparator(clientDefault), LogChainerConstante.SEPARATOR_DEFAULT);
    }

    @Test
    public void getEncodingTypeTest() {
	// checking that the correct value is returned for a custom component
	// part
	assertEquals(component.getEncodingType(clientCustom), customFileEncoding);

	// checking that if we don't have any value for a component part, we
	// still get the correct default value
	assertEquals(component.getEncodingType(clientDefault), LogChainerConstante.ENCODING_TYPE_DEFAULT);
    }
}
