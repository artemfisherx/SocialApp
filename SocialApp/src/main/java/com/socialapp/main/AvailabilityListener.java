package com.socialapp.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityListener {	
	
	private Logger logger = LoggerFactory.getLogger(AvailabilityListener.class);
	
	@EventListener
	public void livenessStateChange(AvailabilityChangeEvent<LivenessState> event)
	{
		logger.info("LivenessState:" + event.getState());					
	}
	
	@EventListener
	public void readinessStateChange(AvailabilityChangeEvent<ReadinessState> event)
	{
		logger.info("ReadinessState:" + event.getState());
	}
}
