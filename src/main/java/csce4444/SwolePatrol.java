package csce4444;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

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

	@RequestMapping("/")
	public String index(Map<String, Object> model) throws IOException {
		int entrances;
		
		Date now = new Date();
		
		entryLookupEngine.createTestFile(now);
		
		try {
			entrances = entryLookupEngine.testModel();
			model.put("entries", entrances);		
		} 
		catch (IOException e) {
			System.err.println("IOException" + e.getMessage());
			model.put("entries", "Sorry, no data is available for this time period.");
		}	
		
		model.put("time", now.toString());
		
		return "index";
	}
}
