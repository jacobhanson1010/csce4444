package csce4444;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

// This class acts as the main web controller. When a web request comes in, it is handled by the appropriate
// RequestMapping method. Starts on port 8080 by default.

@Controller
@SpringBootApplication
public class SwolePatrol {

	// These are the components necessary for the application to function

	@Autowired
	private EntryLookupEngine entryLookupEngine;

	/**
	 * This method is what runs when the application starts. Its purpose is to run
	 * this class as a spring application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Set this class as the Spring Controller
		SpringApplication.run(SwolePatrol.class, args);
	}

	// These are the request mappings for handling web requests.

	@RequestMapping("/")
	public String index(Map<String, Object> model) throws IOException {
		// NEED TO MAKE A TEST FILE IN ARFF BASED ON CURRENT DATE
		// THEN EVALUATE MODEL ON TEST FILE
		// THEN RETURN PREDICTION TO HTML RESPONSE
		// GET CURRENT TIME
		
		Date now = new Date();
		
		entryLookupEngine.createTestFile(now);
		System.out.println("now= " + now);
		
		//try {
		//Double entriesForDate = entryLookupEngine.get(now);
			//model.put("entries", "" + (new DecimalFormat(".00")).format(entriesForDate) /*entriesForDate*/);
		//} catch (NoSuchElementException e) {
//			model.put("entries", "Sorry, no data is available for this time.");
		//}
		
		
		//model.put("time", now.toString());
		
		return "index"; // this is the name of the html file to return
	}

}
