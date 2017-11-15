package csce4444;

//import java.io.File;
//import java.util.ArrayList;
//import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
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
			file = new FileInputStream("C:/eclipse/Book1.xlsx");
		}
		
		HashMap<Date, Integer> map = new HashMap<Date, Integer>();
		}
		
		XSSFSheet sheet = wb.getSheetAt(0);
		XSSFRow row = null;
		XSSFCell cell = null;
		
		Iterator rows = sheet.rowIterator();
		
		while (rows.hasNext()){
			Iterator cells = row.cellIterator();
			while (cells.hasNext()){
				
				if (cell.getCellType() == XSSFCell.CELL_TYPE_DATE) {
					// Not sure how to add to hashmap here
				}
				else if (cell.getCellType() == XSSF.CELL_TYPE_NUMERIC) {
					// Not sure how to add to hashmap here
				}
				cell = (XSSFCell) cells.next();
			}
			
			row = (XSSFRow) rows.next();
		}
				
		try {
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}	
}
