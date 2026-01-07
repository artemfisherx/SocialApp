package com.socialapp.main.ConfigurationProperties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@ConfigurationProperties("my") //префикс свойств
public class ConfigProperties {
	
	private Duration duration = Duration.ofMillis(1); // значение по умолчанию до привязки
	
	private Resource resource;

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

}
