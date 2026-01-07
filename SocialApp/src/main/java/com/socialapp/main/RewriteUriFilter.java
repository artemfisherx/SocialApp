package com.socialapp.main;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/*
 * Используется чтобы избавляться от / в конце requestUri.
 * Вызывается самым первым, до фильтров Spring Security.
 */

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RewriteUriFilter extends OncePerRequestFilter{
	
	private Logger logger = LoggerFactory.getLogger(RewriteUriFilter.class);
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		logger.debug("doFilterInternal HttpServletRequest requestURI:" + request.getRequestURI());
		
		HttpServletRequest newRequest = new MyHttpServletRequestWrapper(request); 			
		filterChain.doFilter(newRequest, response);			
	}				
	
	
	class MyHttpServletRequestWrapper extends HttpServletRequestWrapper
	{
		private HttpServletRequest request;
		
		MyHttpServletRequestWrapper(HttpServletRequest request)
		{
			super(request);
			this.request = request;
		}
		
		@Override
		public String getRequestURI()
		{
			String requestUri = super.getRequestURI();
			
			logger.debug("getRequestURI requestUri:" + requestUri);
			
			//чтобы редирект на WelcomePage по адресу '/' отрабатывал
			if(requestUri.length()<2)
				return requestUri;
			
			String newRequestUri = requestUri;		
			
			if(requestUri.endsWith("/"))
				newRequestUri = requestUri.substring(0, requestUri.length()-1);
			
			logger.debug("getRequestURI newRequestUri:" + newRequestUri);
			
			return newRequestUri;
		}		
		
	}	 

}
