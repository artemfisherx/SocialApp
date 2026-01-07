package com.socialapp.main.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * Используется для удаления сессии из MyJdbcSessionRegistry при выходе из системы
 */
public class MyJdbcSessionRegistryLogoutHandler implements LogoutHandler{
	
	@Autowired
	private MyJdbcSessionRegistry sessionRegistry;
	
	public MyJdbcSessionRegistryLogoutHandler(MyJdbcSessionRegistry sessionRegistry)
	{
		this.sessionRegistry = sessionRegistry;
	}
	
	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, @Nullable Authentication authentication)
	{
		if(request.getSession(false)==null)
			return;
		
		String sessionId = request.getSession(false).getId();
		sessionRegistry.removeSessionInformation(sessionId);
	}

}
