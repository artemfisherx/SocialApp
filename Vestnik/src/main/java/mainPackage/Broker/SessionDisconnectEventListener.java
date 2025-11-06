package mainPackage.Broker;

import org.springframework.context.ApplicationListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import mainPackage.MainLogger;

public class SessionDisconnectEventListener implements ApplicationListener<SessionDisconnectEvent>{
	
	public void onApplicationEvent(SessionDisconnectEvent event)
	{
		MainLogger.logger.info("SessionDisconnectEventListener Disconnected:" + event);
	}

}
