package csce4444;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationException;
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
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
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
	 * It's job is to initialize the model with the provided training data. 
	 * @throws IOException
	 */
	@Autowired
	public EntryLookupEngine() throws IOException {
		// Convert the CSV file to ARFF format
		CSV_to_ARFF("data/All_training_data.csv", "data/training_data.arff");
		
		// Insert training data
		train = createInstance("data/training_data.arff");
	}
	
	/**
	 * Tests classifier on test data, returns prediction
	 * @return Number of entrances predicted by model
	 * @throws IOException
	 */
	public int testModel() throws IOException {
		int entrances;
		
		Classifier model = new DecisionTable();
		
		test = createInstance("data/test_data.arff");
		
		// Build classifier on the training data and evaluate the test data
		classify(model);
		
		// Get the result from the prediction made on the test data
		entrances = getResult(model);
		
		return entrances;
	}
	
	/**
	 * Gets prediction value from model.
	 * @param model Classifier used on training set, testing set inst
	 * @return int Number of entrances in the past 15 minutes
	 * @throws EvaluationException
	 */
	private int getResult(Classifier model) throws EvaluationException {
		double[] prediction = new double[test.numInstances()];

		// Get prediction info for instance 0 of the test set
		try {
			prediction = model.distributionForInstance(test.get(0));
		} catch (Exception e) {
			throw new EvaluationException(e.getMessage());
		}
		
		// Round up to the nearest integer since people aren't fractional...on the outside
		prediction[0] = Math.ceil(prediction[0]);
		
		return (int) prediction[0];
	}
	
	/**
	 * Builds the classifier on the training data and predicts desired values on test data.
	 * @param model Classifier to be used on training set
	 * @throws EvaluationException
	 */
	private void classify(Classifier model) throws EvaluationException {
		// BAD WEKA, BAD
		Evaluation evaluation;
		try {
			evaluation = new Evaluation(train);
			model.buildClassifier(train);
			evaluation.evaluateModel(model, test);
		} catch (Exception e) {
			throw new EvaluationException(e.getMessage());
		}
	}
	
	
	/**
	 * Creates test file based off of arff template and appends date instance to test file.
	 * @param date object to append to testing file
	 * @throws IOException
	 */
	public void createTestFile(Date date) throws IOException {
		
		String outputString;
		String templateFile = "data/template.arff";
		String outputFile = "data/test_data.arff";
		
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

	/**
	 * Formats date object to be compatible with arff file format.
	 * @param date object used to format test file
	 * @return String containing formatted date for arff file
	 */
	public String formatOutputString(Date date) {
		
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
				
		// Ending "time" range
		endDate = cal.getTime();
				
		// Calculate our beginning "time" range
		cal.add(Calendar.MINUTE, -15);
		beginDate = cal.getTime();
				
		// Populate strings for formatting
		endHour = getHourFromDate(endDate);
		endMinutes = getMinutesFromDate(endDate);
				
		// Edge Case: 10:45-10:59PM, we need that 10:59, not 11:00
		if(Integer.parseInt(endMinutes) == 0) {
			endMinutes = String.valueOf(59);
			
			// Add leading '0' after rounding down if needed
			if(Integer.parseInt(endHour) <= 10) {
				endHour = "0" + String.valueOf(Integer.parseInt(endHour) - 1);
			}
			else{
				endHour = String.valueOf(Integer.parseInt(endHour) - 1);
			}
		}
		
		// Populate strings for formatting
		meridiem = getMeridiemFromDate(beginDate);
		beginHour = getHourFromDate(beginDate);
		beginMinutes = getMinutesFromDate(beginDate);
		
		// Output string
		result = month + "," + dayOfWeek + "," + beginHour + ":" + beginMinutes + "-" + endHour + ":" + endMinutes + meridiem + ",?\n";

		return result;
	}
	
	/**
	 * Reads from arff file and creates an Instances obj.
	 * @param filename Name of the file to create the instance from
	 * @return Instances obj of the data read from file
	 * @throws IOException
	 */
	private Instances createInstance(String filename) throws IOException {
		BufferedReader datafile = readDataFile(filename);
		
		// Create a new instance based of off the data
		Instances inst = new Instances(datafile);
		inst.setClassIndex(inst.numAttributes() - 1);
		
		// Close buffer
		datafile.close();
		
		return inst;
	}
	
	/**
	 * Helper method that copies one file to another
	 * @param source Source file 
	 * @param dest destination file
	 * @throws IOException
	 */
	private static void copyFileUsingStream(File source, File dest) throws IOException {
		
		// I/O Streams
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
	
	/**
	 * Helper method that converts a CSV to an ARFF.
	 * @param src Source file 
	 * @param dest destination file
	 * @throws IOException
	 */
	private void CSV_to_ARFF(String src, String dest) throws IOException{
		CSVLoader loader = new CSVLoader();
		
		// Load CSV
		loader.setSource(new File(src));
		Instances data = loader.getDataSet();
		
		// Save ARFF
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File(dest));
		saver.writeBatch();
	}
	
	/**
	 * Helper method for reading files
	 * @param filename Name of the file to attach the reader to
	 * @return BufferedReader to read from file
	 */
	private BufferedReader readDataFile(String filename) {
		BufferedReader inputReader = null;
		
		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}
		
		return inputReader;
	}

	/**
	 *  Helper method to obtain snippet of the date
	 * @param date Date object to parse month from
	 * @return String containing month in "MMM" format
	 */
	public String getMonthFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("MMM");
		return format.format(date);
	}

	/**
	 * Helper method to obtain snippet of the date
	 * @param date Date object to parse day from
	 * @return String containing day in "EEEE" format
	 */
	public String getDayOfWeekFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("EEEE");
		return format.format(date);
	}
	
	/**
	 * Helper method to obtain snippet of the date
	 * @param date Date object to parse hour from
	 * @return String containing hour in "hh" format
	 */
	public String getHourFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("hh");
		return format.format(date);
	}
	
	/**
	 * Helper method to obtain snippet of the date
	 * @param date Date object to parse minutes from
	 * @return String containing minutes in "mm" format
	 */
	public String getMinutesFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("mm");
		return format.format(date);
	}
	
	/**
	 * Helper method to obtain snippet of the date
	 * @param date Date object to parse meridiem(AM/PM) from
	 * @return String containing meridiem in "aa" format
	 */
	public String getMeridiemFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("aa");
		return format.format(date);
	}
}
