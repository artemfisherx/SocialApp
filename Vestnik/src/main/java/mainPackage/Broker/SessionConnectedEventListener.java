package mainPackage.Broker;

import org.springframework.context.ApplicationListener;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import mainPackage.MainLogger;

public class SessionConnectedEventListener implements ApplicationListener<SessionConnectedEvent>{
	
	public void onApplicationEvent(SessionConnectedEvent event)
	{
		MainLogger.logger.info("SessionConnectedEventListener Connected:" + event);
	}

}
