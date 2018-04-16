package ch.ge.cti.logchainer.service;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import ch.ge.cti.logchainer.service.flux.FluxService;
import ch.ge.cti.logchainer.service.flux.FluxServiceImpl;

public class FluxServiceTest {
    private FluxService fluxService = new FluxServiceImpl();
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
