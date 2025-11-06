package mainPackage;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


public class MainLogger {
	public static final Logger logger = Logger.getLogger("main");
	
	static {	
		
		logger.setLevel(Level.ALL);
		logger.setUseParentHandlers(false);
		
		Formatter conFormatter = new Formatter() {
			@Override
			public String format(LogRecord record)
			{
				Instant instant = record.getInstant();
				LocalDateTime dt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
				return record.getMessage() + " " + record.getLevel().getName() + " "
					+ dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")) + "\n";				
			}
		};
		
		ConsoleHandler conHandler = new ConsoleHandler();
		conHandler.setLevel(Level.ALL);
		conHandler.setFormatter(conFormatter);
		
		logger.addHandler(conHandler);
		
		try
		{
			String pattern = System.getProperty("catalina.home") + "/webapps/VestnikLogs/log%u.log";
			Formatter fileFormatter = new Formatter() {
				@Override
				public String format(LogRecord record)
				{
					Instant instant = record.getInstant();
					LocalDateTime dt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
					return dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")) 
							+ " " + record.getLevel().getName() + " " + record.getMessage() + "\n";					
				}
			};
			
			FileHandler fileHandler = new FileHandler(pattern);
			fileHandler.setLevel(Level.ALL);
			fileHandler.setFormatter(fileFormatter);
			
			logger.addHandler(fileHandler);
		}
		catch(IOException ex)
		{
			System.out.println("MainLogger FileHandler error:" + ex.getMessage());
		}
		
		
	}
	
}
