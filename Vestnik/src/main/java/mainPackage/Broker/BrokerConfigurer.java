package mainPackage.Broker;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/*
 * Используется при добавлении новости в ручную (см. AddEditNewsHandler.saveNews(..))
 */

@Configuration
@EnableWebSocketMessageBroker
public class BrokerConfigurer implements WebSocketMessageBrokerConfigurer{
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry reg)
	{
		reg.addEndpoint("/connect");
	}
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry reg)
	{
		reg.enableSimpleBroker("/topic");
	}
	
	@Override
	public void configureClientInboundChannel(ChannelRegistration reg)
	{
		var executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(20);
		executor.setQueueCapacity(20);
		executor.setMaxPoolSize(40);
		executor.initialize();
		reg.taskExecutor(executor);
	}
	
	@Override
	public void configureClientOutboundChannel(ChannelRegistration reg)
	{
		var executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(20);
		executor.setQueueCapacity(20);
		executor.setMaxPoolSize(40);
		executor.initialize();
		reg.taskExecutor(executor);
	}
	
	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration reg)
	{
		reg.setSendTimeLimit(15*1000); //15 seconds
		reg.setMessageSizeLimit(32*1024); //32 KB
		reg.setSendBufferSizeLimit(1024*1024);//1 MB
	}

}
