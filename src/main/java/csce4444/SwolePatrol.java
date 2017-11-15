package csce4444;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

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
	public String index(Map<String, Object> model) {
		
		// GET CURRENT TIME
		
		Date now = new Date();
		Date fifteenMinutesAgo = Date.from((LocalDateTime.now()).minus(15, ChronoUnit.MINUTES).toInstant(ZoneOffset.of("CST")));
		Integer entriesForDate = entryLookupEngine.get(new Date());
		
		///////////////////////////////////////////////////////////////
		
		// replace variables in the html file
		model.put("swipes", "" + entriesForDate /*entriesForDate*/);
		model.put("fifteenMinutesAgo", fifteenMinutesAgo);
		return "index"; // this is the name of the html file to return
	}

}
