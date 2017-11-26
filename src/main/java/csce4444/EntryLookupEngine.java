package csce4444;

import java.util.Calendar;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.DecisionTable;
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
	private Instances train;
	private Instances test;
	
	/**
	 * This constructor is automatically called when the application starts up.
	 * It's job is to initialize the map that contains the entry lookup data. 
	 * @throws Exception 
	 */
	@Autowired
	public EntryLookupEngine() throws Exception {
		//This should only build the model from the training file, nothing more
		//We will have other functions for requesting/building test file
		initializeWekaModel();
		//entryLookupMap = initializeMap();
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
	
	public static void printResults(Classifier model, Instances testingSet) throws Exception {
		// 2 is the instance in the testing set that we are predicting
		double[][] prediction = new double[testingSet.numInstances()][];
		for(int i = 0; i < testingSet.numInstances(); i++) {
			prediction[i] = model.distributionForInstance(testingSet.get(i));
		}
		//double[] prediction = model.distributionForInstance(testingSet.get(2));
		System.out.println("Probability of class " + testingSet.classAttribute().name() + " : ");
		for(int i = 0; i < prediction.length; i++) {
			for(int j = 0; j < prediction[i].length; j++) {
				System.out.println("Instance[" + i + "] = " + testingSet.instance(i).stringValue(0) + " " + testingSet.instance(i).stringValue(1) + " " + testingSet.instance(i).stringValue(2) + " " + Double.toString(prediction[i][j]));
			}
		}
	}
	
	public static Evaluation classify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception {
		Evaluation evaluation = new Evaluation(trainingSet);
		
		model.buildClassifier(trainingSet);
		evaluation.evaluateModel(model, testingSet);

		return evaluation;
	}
	
	public static BufferedReader readDataFile(String filename) {
		BufferedReader inputReader = null;
		
		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}
		
		return inputReader;
	}
	
	private Instances createInstance(String path) throws IOException {
		BufferedReader datafile = readDataFile(path);
		
		Instances inst = new Instances(datafile);
		inst.setClassIndex(inst.numAttributes() - 1);
		
		datafile.close();
		
		return inst;
	}
	
	private String getMonthFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("MMM");
		return format.format(date);
	}
	
	private String getDayOfWeekFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("EEEE");
		return format.format(date);
	}
	
	private String getHourFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("hh");
		return format.format(date);
	}
	
	private String getMinutesFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("mm");
		return format.format(date);
	}
	
	private String getMeridiemFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("aa");
		return format.format(date);
	}
	
	private String formatOutputString(Date date) {
		// Populate the test file with the current date/time
		int mod, numMinutes;
		Date beginDate, endDate;
		String result, beginHour, endHour, beginMinutes, endMinutes, meridiem;
		String month = getMonthFromDate(date);
		String dayOfWeek = getDayOfWeekFromDate(date);
		Calendar cal = Calendar.getInstance();
		
		cal.setTime(date);
		
		numMinutes = cal.get(Calendar.MINUTE);
		mod = numMinutes % 15;
		
		// Round down to the nearest 15
		if (mod == 0) 
			cal.add(Calendar.MINUTE, -15);
		else 
			cal.add(Calendar.MINUTE, -mod);
				
		// That's our end "time" range
		endDate = cal.getTime();
				
		// Now calculate our beginning "time" range
		cal.add(Calendar.MINUTE, -15);
		beginDate = cal.getTime();
				
		// Populate strings for formatting
		endHour = getHourFromDate(endDate);
		endMinutes = getMinutesFromDate(endDate);
				
		// Edge Case: 10:45-10:59PM, we need that 10:59, not 11:00
		if(Integer.parseInt(endMinutes) == 0) {
			endMinutes = String.valueOf(59);
			endHour = String.valueOf(Integer.parseInt(endHour) - 1);
		}
		
		// Populate strings for formatting
		meridiem = getMeridiemFromDate(beginDate);
		beginHour = getHourFromDate(beginDate);
		beginMinutes = getMinutesFromDate(beginDate);
		
		// Output string
		result = month + "," + dayOfWeek + "," + beginHour + ":" + beginMinutes + "-" + endHour + ":" + endMinutes + meridiem + ",?\n";
				
		System.out.println("Full output= " + result);
		
		return result;
	}
	
	public void createTestFile(Date date) throws IOException {
		
		String outputString;
		
		File source = new File("data/template.arff");
		File destination = new File("data/test.arff");
		
		// Remove any old test files
		destination.delete();
		
		// Create the new test file
		destination.createNewFile();
		
		// Copy the template to the test file
		copyFileUsingStream(source, destination);
		
		// Format string for output
		outputString = formatOutputString(date);
	}
	
	private static void copyFileUsingStream(File source, File dest) throws IOException {
		InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
	
	private void initializeWekaModel() throws Exception {
		System.out.println("Initializing model...");
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		
		//Date now = new Date();
		//createTestFile(now);
		
		train = createInstance("data/Alldata_converted.arff");
		test = createInstance("data/test_data.arff");
		
		// The test data needs to be pulled from the site, this is just to make sure it works
		
		Classifier model = new DecisionTable();
		Evaluation result = classify(model, train, test);
		
		printResults(model, test);
		
		//return result;
	}
	
	private LinkedHashMap<Date, Double> initializeMap() throws IOException, ParseException {

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
