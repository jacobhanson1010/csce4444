package csce4444;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestEntryLookupEngine {
	
	Logger logger = Logger.getLogger(this.getClass());
	
	private static EntryLookupEngine entryLookupEngine;
	
	@BeforeClass
	public static void initEntryLookupengine() throws IOException {
		entryLookupEngine = new EntryLookupEngine();
	}
	
	// Testing actual results
	
	@Test (expected = IOException.class)
	public void testTestModel1() throws IOException {
		// invalid date, with no data available
		entryLookupEngine.createTestFile(new Date(1234567890));
		entryLookupEngine.testModel();
	}
	
	@Test
	public void testTestModel2() throws IOException {
		// valid date
		entryLookupEngine.createTestFile(new Date(1264567890));
		entryLookupEngine.testModel();
	}
	
	// Testing helper functions
	
	@Test
	public void testFormatOutputString() {
		String result = entryLookupEngine.formatOutputString(new Date(1234567890));
		assertEquals("Jan,Thursday,12:30-12:45AM,?\n", result);
	}
	
	@Test
	public void testGetMonthFromDate() {
		String result = entryLookupEngine.getMonthFromDate(new Date(1234567890));
		assertEquals("Jan", result);
	}
	
	@Test
	public void testGetDayOfweekFromDate() {
		String result = entryLookupEngine.getDayOfWeekFromDate(new Date(1234567890));
		assertEquals("Thursday", result);
	}
	
	@Test
	public void testGetHourFromDate() {
		String result = entryLookupEngine.getHourFromDate(new Date(1234567890));
		assertEquals("12", result);
	}

	@Test
	public void testGetMinutesFromDate() {
		String result = entryLookupEngine.getMinutesFromDate(new Date(1234567890));
		assertEquals("56", result);
	}
	
	@Test
	public void testGetMeridiemFromDate() {
		String result = entryLookupEngine.getMeridiemFromDate(new Date(1234567890));
		assertEquals("AM", result);
	}
}
