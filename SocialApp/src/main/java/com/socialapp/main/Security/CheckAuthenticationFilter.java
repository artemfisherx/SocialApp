package com.socialapp.main.Security;

import java.io.IOException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.LogoutSuccessEventPublishingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.csrf.CsrfLogoutHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * Используется для выхода пользователя из системы под текущей сессий, если она была закрыта из другой сессии.
 */
public class CheckAuthenticationFilter extends OncePerRequestFilter{
	
	private Logger logger = LoggerFactory.getLogger(CheckAuthenticationFilter.class);
	private SessionRegistry sessionRegistry;
	private DataSource dataSource;
	private ApplicationEventPublisher eventPublisher;
	
	public CheckAuthenticationFilter(SessionRegistry sessionRegistry, DataSource dataSource, ApplicationEventPublisher eventPublisher)
	{
		this.sessionRegistry = sessionRegistry;
		this.dataSource = dataSource;
		this.eventPublisher = eventPublisher;
	}
	
	
	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			 throws jakarta.servlet.ServletException, IOException
	{
		Authentication auth = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();	
		
		logger.debug("request:" + UrlUtils.buildFullRequestUrl(request));
		logger.debug("auth class:" + auth.getClass());
		logger.debug("is auth:"+auth.isAuthenticated());
		
		if(auth.getClass()==AnonymousAuthenticationToken.class)	
		{
			filterChain.doFilter(request, response);
		}
		else
		{
			String sessionId = request.getSession(false).getId();
			SessionInformation info = sessionRegistry.getSessionInformation(sessionId);
			
			logger.debug("info:" + info);
			if(info!=null)
				filterChain.doFilter(request, response);
			else
				this.logout(request, response, auth);
		}		
		
		logger.debug("finish");
		
	}
	
	private void logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) throws ServletException, IOException
	{
		var secContextLogoutHandler = new SecurityContextLogoutHandler();
		
		var username = auth.getName();
		var tokenRepository = new JdbcTokenRepositoryImpl();
		tokenRepository.setDataSource(dataSource);
		
		var csrfTokenRepository = new HttpSessionCsrfTokenRepository();
		var csrfLogoutHandler = new CsrfLogoutHandler(csrfTokenRepository);
		
		var publisher = new LogoutSuccessEventPublishingLogoutHandler();
		publisher.setApplicationEventPublisher(eventPublisher);
		
		var successHandler = new SimpleUrlLogoutSuccessHandler();
		successHandler.setTargetUrlParameter("/");
		
		secContextLogoutHandler.logout(request, response, auth);
		tokenRepository.removeUserTokens(username);
		csrfLogoutHandler.logout(request, response, auth);
		publisher.logout(request, response, auth);
		successHandler.onLogoutSuccess(request, response, auth);
		
		logger.debug("success logout");
	}

}
