package csce4444;

import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	private Instances train;
	private Instances test;
	
	/**
	 * This constructor is automatically called when the application starts up.
	 * It's job is to initialize the model with the provided training data 
	 * @throws Exception 
	 */
	@Autowired
	public EntryLookupEngine() throws Exception {
		System.out.println("Initializing model...");
		train = createInstance("data/Alldata_converted.arff");
	}
	
	/**
	 * This method returns a quantity of entries for the past 15 minutes based on the supplied date.
	 * @param date The date object for the desired entries.
	 * @return The quantity of entries.
	 * @throws NoSuchElementException If an invalid date is specified, then this error will be thrown.
	 */
	
	private static int getResult(Classifier model, Instances testingSet) throws Exception {
		double[] prediction = new double[testingSet.numInstances()];

		prediction = model.distributionForInstance(testingSet.get(0));
		
		prediction[0] = Math.ceil(prediction[0]);
		System.out.println("Instance[0] = " + testingSet.instance(0).stringValue(0) + " " + testingSet.instance(0).stringValue(1) + " " + testingSet.instance(0).stringValue(2) + " " + (int) prediction[0]);
		
		return (int) prediction[0];
	}
	
	private static Evaluation classify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception {
		Evaluation evaluation = new Evaluation(trainingSet);
		
		model.buildClassifier(trainingSet);
		evaluation.evaluateModel(model, testingSet);

		return evaluation;
	}
	
	private static BufferedReader readDataFile(String filename) {
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
		String templateFile = "data/template.arff";
		String outputFile = "data/test.arff";
		
		File source = new File(templateFile);
		File destination = new File(outputFile);
		
		// Remove any old test files
		destination.delete();
		
		// Create the new test file
		destination.createNewFile();
		
		// Copy the template to the test file
		copyFileUsingStream(source, destination);
		
		// Format string for output
		outputString = formatOutputString(date);
		
		// Write to output
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));
		writer.append(outputString);
		
		// Close output buffer
		writer.close();
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
	
	public int evaluateModel() throws Exception {
		int entrances;
		
		Classifier model = new DecisionTable();
		
		test = createInstance("data/test.arff");
		
		classify(model, train, test);
		
		entrances = getResult(model, test);
		
		return entrances;
	}
}
