package com.socialapp.autoconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({Logger.class, LoggerFactory.class})
@ConditionalOnMissingBean(name="mainLogger")
public class LoggerAutoConfiguration {
	
	@Bean
	public Logger mainLogger()
	{
		return LoggerFactory.getLogger("mainLogger");
	}

}
