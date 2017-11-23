package csce4444;

import java.util.Calendar;
//import java.util.ArrayList;
//import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.log4j.Logger;

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
	private Logger logger = Logger.getLogger(this.getClass());
	
	private LinkedHashMap<Date, Double> entryLookupMap;
	
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
	public Double get(Date date) throws NoSuchElementException, IllegalStateException {
		Calendar lookup = Calendar.getInstance();
        lookup.setTime(date);
        
        Calendar key = Calendar.getInstance();
        key.setTime(new Date(0));
        key.set(Calendar.HOUR_OF_DAY, lookup.get(Calendar.HOUR_OF_DAY));
        key.set(Calendar.MINUTE, lookup.get(Calendar.MINUTE));
        
        int unroundedMinutes = key.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 15;
        key.add(Calendar.MINUTE, mod == 0 ? -15 : -mod);
        key.set(Calendar.SECOND, 0);
        logger.info(key.getTime().toString() + " was the rounded time for the request.");
		return entryLookupMap.get(key.getTime());
	}
	
	
	
	private LinkedHashMap<Date, Double> initializeMap() throws IOException, ParseException {
		// TODO DO THIS IBRAHIM

		FileInputStream file = new FileInputStream("FacilityAccessEntranceExitStatisticsReportSpring2017.xlsx");

		LinkedHashMap<Date, Double> map = new LinkedHashMap<Date, Double>();
		LinkedHashMap<Date, Integer> quantity = new LinkedHashMap<Date, Integer>();
		
		XSSFWorkbook wb = new XSSFWorkbook(file);
		XSSFSheet sheet = wb.getSheetAt(0);
		XSSFCell cell = null;

		Iterator<Row> rows = sheet.rowIterator();

		Calendar key = Calendar.getInstance();
		key.setTime(new Date(0));
		
		rowLoop: while (rows.hasNext()) {
			Iterator<Cell> cells = rows.next().cellIterator();
			
			try { cell = (XSSFCell) cells.next(); }
			catch (NoSuchElementException e) { break rowLoop; }
			
			
			try {
				if (cell.getStringCellValue().contains("/") && cell.getStringCellValue().contains("-")) {
					continue rowLoop;
				}
			} catch (Exception e) {
				// This means it wasn't a string row.
			}
			try {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(cell.getDateCellValue());
				if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
					key.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
					continue rowLoop;
				}
				if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
					key.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					continue rowLoop;
				}
				if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
					key.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
					continue rowLoop;
				}
				if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
					key.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
					continue rowLoop;
				}
				if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
					key.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
					continue rowLoop;
				}
				if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
					key.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
					continue rowLoop;
				}
				if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
					key.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
					continue rowLoop;
				}
			} catch (Exception e) {
				// This means it wasn't a day block.
			}
			try {
				String time = cell.getStringCellValue();
				time = time.substring(0, 5) + time.substring(11);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mma");
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(simpleDateFormat.parse(time));
				
				key.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
				key.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
				Double valueForTime = cells.next().getNumericCellValue();
				
				Date date = key.getTime();
				
				if (map.containsKey(date)) {
					Double value = map.get(date);
					Integer quan = quantity.get(date);
					map.replace(date, value+valueForTime);
					quantity.replace(date, quan+1);
				}
				else {
					map.put(date, valueForTime);
					quantity.put(date, 1);
				}
				
			} catch (Exception e) {
				// This means it wasn't a time row.
			}
			
		}
		
		for (Map.Entry<Date, Double> entry : map.entrySet()) {
			Date entryKey = entry.getKey();
			Double entryValue = entry.getValue();
			map.replace(entryKey, entryValue/quantity.get(entryKey));
		}
		logger.info("Initialized entryLookupMap with " + quantity.size() + " unique time periods");
		wb.close();
		file.close();
		return map;
	}	
}
