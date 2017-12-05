package csce4444;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// This class acts as the main web controller. When a web request comes in, it is handled by the appropriate
// RequestMapping method. Starts on port 8080 by default.

@Controller
@SpringBootApplication
public class SwolePatrol {
	Logger logger = Logger.getLogger(this.getClass());
	
	// These are the components necessary for the application to function
	@Autowired
	private EntryLookupEngine entryLookupEngine;

	public SwolePatrol() throws IOException {
		entryLookupEngine = new EntryLookupEngine();
	}
	
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

	private boolean customDate = false;
	private Date date;
	
	@RequestMapping("/")
	public String index(Model model) throws IOException {
		int entrances;
		
		if (customDate == false) {
			date = new Date();
			entryLookupEngine.createTestFile(date);
		}
		if (customDate == true) {
			entryLookupEngine.createTestFile(date);
			customDate = false;
		}
		
		try {
			entrances = entryLookupEngine.testModel();
			model.addAttribute("entries", entrances);
			double percent = entrances / 44.0;
			percent *= 100;
			percent = percent > 100 ? 100 : percent;
			model.addAttribute("percent", "width:" + percent + "%");
		} 
		catch (IOException e) {
			System.err.println("IOException" + e.getMessage());
			model.addAttribute("entries", "none");
		}	
		
		model.addAttribute("time", date.toString());
		model.addAttribute("dateContainer", new DateContainer());
		return "index";
	}
	
	@PostMapping("/acceptDateTime")
	public String acceptDateTime(@ModelAttribute DateContainer dc) {
		try {
			date = Date.from(dc.getDateTime().atZone(ZoneId.systemDefault()).toInstant());
			customDate = true;
			return "redirect:/";
		} catch (NullPointerException e) {
			return "redirect:/";
		}
	}
}
