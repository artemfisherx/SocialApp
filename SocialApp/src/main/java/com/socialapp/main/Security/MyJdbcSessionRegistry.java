package com.socialapp.main.Security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import com.socialapp.main.MainRepository;

import jakarta.servlet.http.HttpServletRequest;

/*
 * Expired sessions не удаляются. Только фильтруются при необходимости.
 * При выходе из системы сессия удаляется (см MyJdbcSessionRegistryLogoutHandler).
 */

@Component
public class MyJdbcSessionRegistry implements SessionRegistry	{
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private MainRepository repo;
	
	@Override
	public List<Object> getAllPrincipals()
	{
		List<MyUserDetails> list =  repo.selectAllSessionPrincipals();			
		List<Object> objectList = new ArrayList<>();
		
		for(Object obj:list)
			objectList.add(obj);
		
		return objectList;
	}
	
	public List<MySessionInformation> getAllActiveMySessions(Object principal)
	{	
		if(!(principal instanceof MyUserDetails details))			
			throw new ClassCastException("principal in not MyUserDetails class");
		
		int userId = details.getId();
		
		return repo.selectAllActiveMySessionInfo(userId);
	}
	
	@Override
	public List<SessionInformation> getAllSessions(Object principal, boolean includeExpiredSessions)
	{	
		if(!(principal instanceof MyUserDetails details))			
			throw new ClassCastException("principal in not MyUserDetails class");
		
		int userId = details.getId();
		
		return repo.selectAllSessionInfo(userId, includeExpiredSessions);
	}
	
	@Nullable 
	@Override
	public MySessionInformation getSessionInformation(String sessionId)
	{
		System.out.println("sessionId:" + sessionId);
		return repo.selectMySessionInfo(sessionId);
	}
	
	@Override
	public void refreshLastRequest(String sessionId)
	{
		Date lastRequest = new Date();
		repo.updateUserSession(sessionId, lastRequest);
	}
	
	@Override
	public void registerNewSession(String sessionId, Object principal)
	{
		int userId = 0;
		
		if(principal instanceof MyUserDetails details)
			userId = details.getId();
		else
			throw new ClassCastException("principal in not MyUserDetails class");
		
		Date date = new Date();
		
		String ip = request.getRemoteAddr();
		
		repo.insertUserSession(userId, sessionId, date, ip);
	}
	
	@Override
	public void removeSessionInformation(String sessionId)
	{
		repo.deleteUserSession(sessionId);
	}
	


}
