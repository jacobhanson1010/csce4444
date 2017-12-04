package csce4444;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;

public class TestSwolePatrol {
	
	Logger logger = Logger.getLogger(this.getClass());
	
	private static SwolePatrol swolePatrol;
	
	@BeforeClass
	public static void initEntryLookupengine() throws IOException {
		swolePatrol = new SwolePatrol();
	}
	
	@Test
	public void testIndex() throws IOException {
		String result = swolePatrol.index(new ExtendedModelMap());
		assertEquals("index", result);
	}
	
}
