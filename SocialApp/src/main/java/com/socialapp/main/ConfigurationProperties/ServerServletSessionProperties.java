package com.socialapp.main.ConfigurationProperties;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("server.servlet.session") //префикс свойств
public class ServerServletSessionProperties {	
	
	private Duration timeout;

	public Duration getTimeout() {
		return timeout;
	}

	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	} 

}
