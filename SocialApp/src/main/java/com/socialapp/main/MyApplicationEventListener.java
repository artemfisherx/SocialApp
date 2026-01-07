package com.socialapp.main;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Profile("debug")
@Component
public class MyApplicationEventListener {
	
	@Autowired
	BufferingApplicationStartup startup;
	
	private Logger logger = LoggerFactory.getLogger(MyApplicationEventListener.class);
	
	@EventListener(AvailabilityChangeEvent.class)
	void handleAvailabilityChangeEvent()
	{
		startup.getBufferedTimeline().getEvents().forEach(e->{
			
			String name= e.getStartupStep().getName();
			Duration duration = e.getDuration();	
			logger.info(name+ " - " +duration.toString());			
		});
	}

}
