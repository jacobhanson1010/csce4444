package csce4444;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This component's purpose is to do whatever is necessary to retrieve the most up-to-date swipe data from
 * the Pohl Recreation Center, and return it.
 * 
 * @author jeb
 */
@Component
public class EntryLookupEngine {
	
	private HashMap<Date, Integer> entryLookupMap;
	
	/**
	 * This constructor is automatically called when the application starts up.
	 * It's job is to initialize the map that contains the entry lookup data. 
	 */
	@Autowired
	public EntryLookupEngine() {
		entryLookupMap = initializeMap();
	}

	
	/**
	 * This method returns a quantity of entries for the past 15 minutes based on the supplied date.
	 * @param date The date object for the desired entries.
	 * @return The quantity of entries.
	 * @throws NoSuchElementException If an invalid date is specified, then this error will be thrown.
	 */
	public Integer get(Date date) throws NoSuchElementException, IllegalStateException {
		return entryLookupMap.get(date);
	}
	
	
	
	private HashMap<Date, Integer> initializeMap() {
		// TODO DO THIS IBRAHIM
		
		File file = new File("excelfile.whatever");
		
		HashMap<Date, Integer> map = new HashMap<Date, Integer>();
		
		Date date = new Date();
		date.getDay();
		date.getHours();
		
		
		return map;
	}
	
	
}
