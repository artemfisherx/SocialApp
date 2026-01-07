package com.socialapp.main;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebConfiguration implements WebMvcConfigurer{
	
	@Override
	public void addInterceptors(InterceptorRegistry registry)
	{
		registry.addInterceptor(localeChangeInterceptor());
	}
	
	@Bean
	LocaleChangeInterceptor localeChangeInterceptor()
	{
		var interceptor = new LocaleChangeInterceptor();
		interceptor.setParamName("lang");
		return interceptor;
	}
	
	@Bean
	LocaleResolver localeResolver()
	{
		return new SessionLocaleResolver();		
	}
		
	
	@Bean
	@FilterRegistration(urlPatterns="/unsecured/*")
	public Filter filter()
	{
		return new Filter() {
			
			private Logger logger = LoggerFactory.getLogger("UnsecuredFilterLogger");
			
			@Override
			public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
			{
				logger.debug("Request is secure:" + request.isSecure());

				HttpServletResponse resp = (HttpServletResponse) response;				
				
				resp.addCookie(new Cookie("my","test"));				
				
				chain.doFilter(request, resp);
			}
		};

	}
	
	@Bean
	public PasswordEncoder passwordEncoder()
	{		
		DelegatingPasswordEncoder encoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
		encoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
		return encoder;
		//return new BCryptPasswordEncoder();
	}	

}
