package csce4444;

import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Component;

/**
 * This component's purpose is to do whatever is necessary to retrieve the most up-to-date swipe data from
 * the Pohl Recreation Center, and return it.
 * 
 * @author jeb
 */
@Component
public class UNTDataCollector {
	
	public ArrayList<Object> getSwipesForDate(Date date) throws NoSuchElementException {
		return null;
	}
}
