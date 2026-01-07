package com.socialapp.main.Security;

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

import com.socialapp.main.MainRepository;

/*
 * Обрабатывает события аутентификации и выхода из системы
 */

@Component
public class AuthenticationEventListener {
	
	@Autowired
	private MainRepository repo;
	
	private Logger logger = LoggerFactory.getLogger(AuthenticationEventListener.class);
		
	@EventListener
	public void onSuccess(AuthenticationSuccessEvent event)
	{		
		String username = event.getAuthentication().getName();
		logger.debug(username + " has been authenticated successfully");
		repo.insertAuthLogEntry(username, "login", OffsetDateTime.now(), true);
	}
	
	@EventListener
	public void onFail(AbstractAuthenticationFailureEvent event)
	{		
		String username = event.getAuthentication().getName();
		String message = event.getException().getMessage();
		logger.debug("Authentication error: " + message);
		repo.insertAuthLogEntry(username, message, OffsetDateTime.now(), false);
	}
	
	@EventListener
	public void onLogout(LogoutSuccessEvent event)
	{
		String username = event.getAuthentication().getName();
		logger.debug("Success logout: " + username);
		repo.insertAuthLogEntry(username, "logout", OffsetDateTime.now(), true);
	}

}
