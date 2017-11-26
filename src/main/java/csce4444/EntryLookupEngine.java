package csce4444;

import java.util.Calendar;
import java.util.Date;

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
	 * It's job is to initialize the model with the provided training data 
	 * @throws Exception 
	 */
	/******************************************************************* 
	* Function     : EntryLookupEngine                                 *
	* Parameter(s) : N/A											   *
	* Return-Value : N/A									           *
	* Description  : Initializes Instance of training data when the    *
	* 				 server is started up							   *
	********************************************************************/
	@Autowired
	public EntryLookupEngine() throws Exception {
		// Convert the CSV file to ARFF format
		CSV_to_ARFF("data/All_training_data.csv", "data/training_data.arff");
		
		// Insert training data
		train = createInstance("data/training_data.arff");
	}
	
	/******************************************************************* 
	* Function     : evaluateModel                                     *
	* Parameter(s) : N/A									           *
	* Return-Value : Number of entrances predicted by model            *
	* Description  : Tests classifier on test data, returns prediction *
	********************************************************************/
	public int testModel() throws Exception {
		int entrances;
		
		Classifier model = new DecisionTable();
		
		test = createInstance("data/test_data.arff");
		
		// Build classifier on the training data and evaluate the test data
		classify(model);
		
		// Get the result from the prediction made on the test data
		entrances = getResult(model);
		
		return entrances;
	}
	
	
	/******************************************************************* 
	* Function     : getResult                                         *
	* Parameter(s) : Classifier used on training set, testing set inst *
	* Return-Value : Number of entrances in the past 15 minutes        *
	* Description  : Gets prediction value from model                  *
	********************************************************************/
	private int getResult(Classifier model) throws Exception {
		double[] prediction = new double[test.numInstances()];

		// Get prediction info for instance 0 of the test set
		prediction = model.distributionForInstance(test.get(0));
		
		// Round up to the nearest integer since people aren't fractional...on the outside
		prediction[0] = Math.ceil(prediction[0]);
		
		return (int) prediction[0];
	}
	
	
	/******************************************************************* 
	* Function     : classify                                          *
	* Parameter(s) : Classifier to be used on training set             *
	* Return-Value : N/A                                               *
	* Description  : Builds the classifier on the training data and    *
	* 				 predicts desired values on test data              *
	********************************************************************/
	private void classify(Classifier model) throws Exception {
		Evaluation evaluation = new Evaluation(train);
		
		model.buildClassifier(train);
		evaluation.evaluateModel(model, test);
	}
	
	
	/******************************************************************* 
	* Function     : createTestFile                                    *
	* Parameter(s) : Date object to append to testing file	           *
	* Return-Value : N/A									           *
	* Description  : Creates test file based off of arff template and  *
	* 				 appends date instance to test file			       *
	********************************************************************/
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
	
	
	/******************************************************************* 
	* Function     : formatOutputString                                *
	* Parameter(s) : Date object used to format test file              *
	* Return-Value : String containing formatted date for arff file    *
	* Description  : Formats date object to be compatible with arff    *
	* 				 file format       								   *
	********************************************************************/
	private String formatOutputString(Date date) {
		
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
			endHour = String.valueOf(Integer.parseInt(endHour) - 1);
		}
		
		// Populate strings for formatting
		meridiem = getMeridiemFromDate(beginDate);
		beginHour = getHourFromDate(beginDate);
		beginMinutes = getMinutesFromDate(beginDate);
		
		// Output string
		result = month + "," + dayOfWeek + "," + beginHour + ":" + beginMinutes + "-" + endHour + ":" + endMinutes + meridiem + ",?\n";

		return result;
	}
	

	/******************************************************************* 
	* Function     : createInstance                                    *
	* Parameter(s) : Name of the file to create the instance from      *
	* Return-Value : Instances obj of the data read from file          *
	* Description  : Reads from arff file and creates an Instances obj *
	********************************************************************/
	private Instances createInstance(String filename) throws IOException {
		BufferedReader datafile = readDataFile(filename);
		
		// Create a new instance based of off the data
		Instances inst = new Instances(datafile);
		inst.setClassIndex(inst.numAttributes() - 1);
		
		// Close buffer
		datafile.close();
		
		return inst;
	}
	
	
	/******************************************************************* 
	* Function     : copyFileUsingStream                               *
	* Parameter(s) : Source file and destination file		           *
	* Return-Value : N/A									           *
	* Description  : Helper method that copies one file to another     *
	********************************************************************/
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
	
	private static void CSV_to_ARFF(String src, String dest) throws IOException{
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
	
	
	/******************************************************************* 
	* Function     : readDataFile                                      *
	* Parameter(s) : Name of the file to attach the reader to          *
	* Return-Value : BufferedReader to read from file                  *
	* Description  : Helper method for reading files                   *
	********************************************************************/
	private BufferedReader readDataFile(String filename) {
		BufferedReader inputReader = null;
		
		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}
		
		return inputReader;
	}

	
	/******************************************************************* 
	* Function     : getMonthFromDate                                  *
	* Parameter(s) : Date object to parse month from		           *
	* Return-Value : String containing month in "MMM" format           *
	* Description  : Helper method to obtain snippet of the date       *
	********************************************************************/
	private String getMonthFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("MMM");
		return format.format(date);
	}
	
	
	/******************************************************************* 
	* Function     : getDayOfWeekFromDate                              *
	* Parameter(s) : Date object to parse day from   		           *
	* Return-Value : String containing day in "EEEE" format            *
	* Description  : Helper method to obtain snippet of the date       *
	********************************************************************/
	private String getDayOfWeekFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("EEEE");
		return format.format(date);
	}
	
	
	/******************************************************************* 
	* Function     : getHourFromDate                                   *
	* Parameter(s) : Date object to parse hour from		               *
	* Return-Value : String containing hour in "hh" format             *
	* Description  : Helper method to obtain snippet of the date       *
	********************************************************************/
	private String getHourFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("hh");
		return format.format(date);
	}
	
	
	/******************************************************************* 
	* Function     : getMinutesFromDate                                *
	* Parameter(s) : Date object to parse minutes from		           *
	* Return-Value : String containing minutes in "mm" format          *
	* Description  : Helper method to obtain snippet of the date       *
	********************************************************************/
	private String getMinutesFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("mm");
		return format.format(date);
	}
	
	
	/******************************************************************* 
	* Function     : getMeridiemFromDate                               *
	* Parameter(s) : Date object to parse meridiem(AM/PM) from	       *
	* Return-Value : String containing meridiem in "aa" format         *
	* Description  : Helper method to obtain snippet of the date       *
	********************************************************************/
	private String getMeridiemFromDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("aa");
		return format.format(date);
	}
}
