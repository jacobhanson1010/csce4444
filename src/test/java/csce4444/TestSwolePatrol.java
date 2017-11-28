package csce4444;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSwolePatrol {
	
	Logger logger = Logger.getLogger(this.getClass());
	
	private static SwolePatrol swolePatrol;
	
	@BeforeClass
	public static void initEntryLookupengine() {
		swolePatrol = new SwolePatrol();
	}
	
	@Test
	public void testIndex() throws IOException {
		String result = swolePatrol.index(new HashMap<String, Object>());
		assertEquals("index", result);
	}
	
}
