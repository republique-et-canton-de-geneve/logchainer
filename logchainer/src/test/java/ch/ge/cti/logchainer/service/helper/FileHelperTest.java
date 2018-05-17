package ch.ge.cti.logchainer.service.helper;

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
import ch.ge.cti.logchainer.service.helper.FileHelper;

public class FileHelperTest {
    private final String customSeparator = "-";
    private final String customSortingType = "alphabetical";
    private final String customStampPosition = "before";
    private final String customFileEncoding = "ISO-8859-1";

    private Client clientCustom;
    private Client clientDefault;
    private final FileHelper fileHelper = new FileHelper();

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
    public void the_sorter_should_comply_with_a_format() {
	// check that the correct value is returned for a custom fileHelper
	// part
	assertEquals(fileHelper.getSorter(clientCustom), customSortingType, "wrong custom sorter");

	// check that if we don't have any value for a fileHelper part, we
	// still get the correct default value
	assertEquals(fileHelper.getSorter(clientDefault), SORT_DEFAULT, "wrong default sorter");
    }

    @Test
    public void the_separator_should_comply_with_a_format() {
	// check that the correct value is returned for a custom fileHelper
	// part
	assertEquals(fileHelper.getSeparator(clientCustom), customSeparator, "wrong custom separator");

	// check that if we don't have any value for a fileHelper part, we
	// still get the correct default value
	assertEquals(fileHelper.getSeparator(clientDefault), SEPARATOR_DEFAULT, "wrong default separator");
    }

    @Test
    public void the_stamp_position_should_comply_with_a_format() {
	// check that the correct value is returned for a custom fileHelper
	// part
	assertEquals(fileHelper.getStampPosition(clientCustom), customStampPosition, "wrong custom stamp position");

	// check that if we don't have any value for a fileHelper part, we
	// still get the correct default value
	assertEquals(fileHelper.getStampPosition(clientDefault), STAMP_POSITION_DEFAULT,
		"wrong default stamp position");
    }

    @Test
    public void the_encoding_type_should_comply_with_a_format() {
	// check that the correct value is returned for a custom fileHelper
	// part
	assertEquals(fileHelper.getEncodingType(clientCustom), customFileEncoding, "wrong custom encoding type");

	// check that if we don't have any value for a fileHelper part, we
	// still get the correct default value
	assertEquals(fileHelper.getEncodingType(clientDefault), ENCODING_TYPE_DEFAULT, "wrong default encoding type");
    }
}
