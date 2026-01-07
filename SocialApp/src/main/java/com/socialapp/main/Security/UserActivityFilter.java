package com.socialapp.main.Security;

import java.io.IOException;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.socialapp.main.MainRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * Сохраняет requests аутентифицированных пользователей. Должен вызываться после AuthorizationFilter.
 * Используется, в частности, для отображения пользователей в сети 
 * (как вариант отображение в сети еще можно было сделать с помощью SessionRegistry).
 */
public class UserActivityFilter extends OncePerRequestFilter{
	
	private MainRepository repo;	
	private Logger logger = LoggerFactory.getLogger(UserActivityFilter.class);	
	
	public UserActivityFilter(MainRepository repo)
	{
		this.repo = repo;		
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			  throws jakarta.servlet.ServletException, IOException
	{
		String fullRequest = request.getRequestURI() + "?" + request.getQueryString();
		Object principal = SecurityContextHolder.getContextHolderStrategy()
											.getContext().getAuthentication().getPrincipal();
		
		if(principal instanceof MyUserDetails details)
		{
			int userId = details.getId();
			OffsetDateTime dt = OffsetDateTime.now();
			
			repo.insertUserRequest(userId, fullRequest, dt);
			
			logger.debug("success");
		}		
		
		filterChain.doFilter(request, response);		
	}

}
