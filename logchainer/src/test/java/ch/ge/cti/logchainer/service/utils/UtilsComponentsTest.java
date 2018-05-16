package ch.ge.cti.logchainer.service.utils;

import static ch.ge.cti.logchainer.constant.LogChainerConstant.ENCODING_TYPE_DEFAULT;
import static ch.ge.cti.logchainer.constant.LogChainerConstant.SEPARATOR_DEFAULT;
import static ch.ge.cti.logchainer.constant.LogChainerConstant.SORT_DEFAULT;
import static ch.ge.cti.logchainer.constant.LogChainerConstant.STAMP_POSITION_DEFAULT;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.generate.FilePattern;

public class UtilsComponentsTest {
    private final String customSeparator = "-";
    private final String customSortingType = "alphabetical";
    private final String customStampPosition = "before";
    private final String customFileEncoding = "ISO-8859-1";

    private Client clientCustom;
    private Client clientDefault;
    private final UtilsComponents component = new UtilsComponentsImpl();

    @BeforeClass
    public void setUp() {
	ClientConf clientConfCustom = new ClientConf();
	FilePattern filePatternCustom = new FilePattern();

	filePatternCustom.setSeparator(customSeparator);
	filePatternCustom.setSortingType(customSortingType);
	filePatternCustom.setStampPosition(customStampPosition);

	clientConfCustom.setFilePattern(filePatternCustom);
	clientConfCustom.setClientId("ClientCustom");
	clientConfCustom.setFileEncoding(customFileEncoding);

	clientCustom = new Client(clientConfCustom);

	ClientConf clientConfDefault = new ClientConf();
	FilePattern filePatternDefault = new FilePattern();

	filePatternDefault.setSeparator("");
	filePatternDefault.setSortingType("");
	filePatternDefault.setStampPosition("");

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
	assertEquals(component.getSorter(clientDefault), SORT_DEFAULT);
    }

    @Test
    public void getSeparatorTest() {
	// checking that the correct value is returned for a custom component
	// part
	assertEquals(component.getSeparator(clientCustom), customSeparator);

	// checking that if we don't have any value for a component part, we
	// still get the correct default value
	assertEquals(component.getSeparator(clientDefault), SEPARATOR_DEFAULT);
    }

    @Test
    public void getStampPositionTest() {
	// checking that the correct value is returned for a custom component
	// part
	assertEquals(component.getStampPosition(clientCustom), customStampPosition);

	// checking that if we don't have any value for a component part, we
	// still get the correct default value
	assertEquals(component.getStampPosition(clientDefault), STAMP_POSITION_DEFAULT);
    }

    @Test
    public void getEncodingTypeTest() {
	// checking that the correct value is returned for a custom component
	// part
	assertEquals(component.getEncodingType(clientCustom), customFileEncoding);

	// checking that if we don't have any value for a component part, we
	// still get the correct default value
	assertEquals(component.getEncodingType(clientDefault), ENCODING_TYPE_DEFAULT);
    }
}
