package ch.ge.cti.logchainer.service;

import static org.testng.Assert.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.service.flux.FluxService;

public class FluxServiceTest {
    @Autowired
    private FluxService fluxService;
    
    private String testFilename = "fluxTest_stampTest.txt";

    @Test(description = "testing the method getting the flux name")
    public void testGetFluxName() {
	String fluxname = fluxService.getFluxName(testFilename, "_");
	
	assertEquals(fluxname, "fluxTest");
    }
    
    @Test(description = "testing the method getting the stamp used to sort files")
    public void testGetSortingStamp() {
	String stamp = fluxService.getSortingStamp(testFilename, "_");
	
	assertEquals(stamp, "stampTest");
    }
}
