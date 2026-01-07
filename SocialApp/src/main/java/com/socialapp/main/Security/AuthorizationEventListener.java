package com.socialapp.main.Security;

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.authorization.method.MethodInvocationResult;
import org.springframework.stereotype.Component;

import com.socialapp.main.MainRepository;

import jakarta.servlet.http.HttpServletRequest;

/*
 * Обрабатывает события авторизации
 */

@Component
public class AuthorizationEventListener {
	
	@Autowired
	private MainRepository repo;
	
	private Logger logger = LoggerFactory.getLogger(AuthorizationEventListener.class);
	
!!	сделать публикацию и обработку события AuthorizationGrantedEvent
	
	//события авторизации для @PreAuthorize-методов
	@EventListener
	public void onPreMethodDenied(AuthorizationDeniedEvent<ReflectiveMethodInvocation> event)
	{
		boolean isSuccess = event.getAuthorizationResult().isGranted();
		String username = event.getAuthentication().get().getName();
		String methodClass = event.getObject().getMethod().getDeclaringClass().toString();
		String methodName = event.getObject().getMethod().getName();
		String desc = methodClass + " " + methodName;		
			
		logger.debug("Authorization denied for " + username);
		repo.insertAuthzLogEntry(username, desc, OffsetDateTime.now(), isSuccess);		
	}	
	
	//события авторизации для @PostAuthorize-методов
	@EventListener
	public void onPostMethodDenied(AuthorizationDeniedEvent<MethodInvocationResult> event)
	{
		boolean isSuccess = event.getAuthorizationResult().isGranted();
		String username = event.getAuthentication().get().getName();
		String methodClass = event.getObject().getMethodInvocation().getMethod().getDeclaringClass().toString();
		String methodName = event.getObject().getMethodInvocation().getMethod().getName();
		String desc = methodClass + " " + methodName;		
		
		logger.debug("Authorization denied for " + username);
		repo.insertAuthzLogEntry(username, desc, OffsetDateTime.now(), isSuccess);		
	}
	
	//события авторизации на уровне запроса
	@EventListener
	public void onRequestDenied(AuthorizationDeniedEvent<? extends HttpServletRequest> event)
	{
		var trustResolver = new AuthenticationTrustResolverImpl();
		var auth = event.getAuthentication().get();		
		
		if(trustResolver.isAnonymous(auth))
			return;
		
		boolean isSuccess = event.getAuthorizationResult().isGranted();
		String username = auth.getName();
		String desc = event.getObject().getRequestURI();
		repo.insertAuthzLogEntry(username, desc, OffsetDateTime.now(), isSuccess);
	}
	
}
