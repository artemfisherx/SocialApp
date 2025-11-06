package mainPackage.EventHandlers;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import mainPackage.MainLogger;

@Component
public class ContextEventsHandler {
	
	@EventListener
	public void refreshedEventHandler(ContextRefreshedEvent event)
	{
		MainLogger.logger.fine("+++ ContextEventsHandler refreshedEventHandler: " + event);
	}
	
	@EventListener
	public void startedEventHandler(ContextStartedEvent event)
	{
		MainLogger.logger.fine("+++ ContextEventsHandler startedEventHandler: " + event);
	}
	
	@EventListener
	public void stoppedEventHandler(ContextStoppedEvent event)
	{
		MainLogger.logger.fine("+++ ContextEventsHandler stoppedEventHandler: " + event);
	}
	
	@EventListener
	public void closedEventHandler(ContextClosedEvent event)
	{
		MainLogger.logger.fine("+++ ContextEventsHandler closedEventHandler: " + event);
	}
	
	@EventListener
	public void servletRequestHandledEventHandler(ServletRequestHandledEvent event)
	{
		MainLogger.logger.fine("+++ ContextEventsHandler servletRequestHandledEventHandler: " + event);
	}
	

}
