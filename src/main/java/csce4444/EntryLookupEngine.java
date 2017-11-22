package csce4444;

import java.util.Calendar;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	 * @throws ParseException 
	 * @throws IOException 
	 */
	@Autowired
	public EntryLookupEngine() throws IOException, ParseException {
		entryLookupMap = initializeMap();
	}

	
	/**
	 * This method returns a quantity of entries for the past 15 minutes based on the supplied date.
	 * @param date The date object for the desired entries.
	 * @return The quantity of entries.
	 * @throws NoSuchElementException If an invalid date is specified, then this error will be thrown.
	 */
	public Integer get(Date date) throws NoSuchElementException, IllegalStateException {
		Calendar lookup = Calendar.getInstance();
        lookup.setTime(date);
        
        Calendar key = Calendar.getInstance();
        key.setTime(new Date(0));
        key.set(Calendar.HOUR_OF_DAY, lookup.get(Calendar.HOUR_OF_DAY));
        key.set(Calendar.MINUTE, lookup.get(Calendar.MINUTE));
        
        int unroundedMinutes = key.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 5;
        key.add(Calendar.MINUTE, unroundedMinutes == 0 ? -15 : -mod);
        key.set(Calendar.SECOND, 0);
		return entryLookupMap.get(key.getTime());
	}
	
	
	
	private HashMap<Date, Integer> initializeMap() throws IOException, ParseException {
		// TODO DO THIS IBRAHIM

		FileInputStream file = new FileInputStream("Book1.xlsx");
		
		HashMap<Date, Integer> map = new HashMap<Date, Integer>();
		
		XSSFWorkbook wb = new XSSFWorkbook(file);
		XSSFSheet sheet = wb.getSheetAt(0);
		XSSFRow row = null;
		XSSFCell cell = null;
		
		Iterator<Row> rows = sheet.rowIterator();
		
		while (rows.hasNext()){
			Iterator<Cell> cells = rows.next().cellIterator();
			
			cell = (XSSFCell) cells.next();
			Calendar key = Calendar.getInstance();
			key.setTime(new Date(0));
			if (cell.getStringCellValue().equals("Sunday"))
				key.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			if (cell.getStringCellValue().equals("Monday"))
				key.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			if (cell.getStringCellValue().equals("Tuesday"))
				key.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
			if (cell.getStringCellValue().equals("Wednesday"))
				key.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
			if (cell.getStringCellValue().equals("Thursday"))
				key.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
			if (cell.getStringCellValue().equals("Friday"))
				key.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
			if (cell.getStringCellValue().equals("Saturday"))
				key.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			
			cell = (XSSFCell) cells.next();
			Calendar timeOfDay = Calendar.getInstance();
			timeOfDay.setTime(cell.getDateCellValue());
			
			key.set(Calendar.HOUR_OF_DAY, timeOfDay.get(Calendar.HOUR_OF_DAY));
			key.set(Calendar.MINUTE, timeOfDay.get(Calendar.MINUTE));
			
			cell = (XSSFCell) cells.next();
			Integer value = (Integer) (int) cell.getNumericCellValue();
			
			System.out.println(""+key.getTime() + " " + value);
			
			map.put(key.getTime(), value);
		}
		wb.close();
		file.close();
		return map;
	}	
}
