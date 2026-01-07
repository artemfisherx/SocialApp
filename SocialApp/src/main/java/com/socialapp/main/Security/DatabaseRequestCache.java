package com.socialapp.main.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.savedrequest.SimpleSavedRequest;
import org.springframework.security.web.session.HttpSessionIdChangedEvent;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.socialapp.main.MainRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * Сохраняет запрос в базе. Привязка идет к id сессии.
 * В ApplicationContext должен присутствовать бин типа HttpSessionEventPublisher (по умолчанию отсутствует).
 */
@Component
public class DatabaseRequestCache implements RequestCache
{
	private Logger logger = LoggerFactory.getLogger(DatabaseRequestCache.class);	
	
	private MainRepository repo;
	
	private String matchingRequestParameterName = "continue";
	
	public DatabaseRequestCache(MainRepository repo)
	{
		this.repo = repo;
	}
	
	//вызывается в RequestCacheAwareFilter 
	@Override
	@Nullable
	public HttpServletRequest getMatchingRequest(HttpServletRequest request, HttpServletResponse response)
	{		
		
		if (this.matchingRequestParameterName != null) {
			if (!StringUtils.hasText(request.getQueryString())
					|| !UriComponentsBuilder.fromUriString(UrlUtils.buildRequestUrl(request))
						.build()
						.getQueryParams()
						.containsKey(this.matchingRequestParameterName)) {
				this.logger.debug(
						"matchingRequestParameterName is required for getMatchingRequest to lookup a value, but not provided");
				return null;
			}
		}
		
		
		if(request.getSession(false)==null)
			return null;
		
		String sessionId = request.getSession(false).getId();
		
		String uri = repo.selectRequestCache(sessionId);
		
		if(uri==null)
			return null;
		
		SavedRequest saved = new SimpleSavedRequest(uri);		
		
		if (!this.matchesSavedRequest(request, saved)) {
			this.logger.debug("Did not match request to the saved one ");			
			return null;
		}
		removeRequest(request, response);		
		
		this.logger.debug("Loaded matching saved request");
		
		return new SavedRequestAwareWrapper(saved, request);		
		
	}
	
	private boolean matchesSavedRequest(HttpServletRequest request, SavedRequest savedRequest) {
		String currentUrl = UrlUtils.buildFullRequestUrl(request);
		return savedRequest.getRedirectUrl().equals(currentUrl);
	}
	
	@Override
	@Nullable
	public SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response)
	{	
		if(request.getSession(false)==null)
			return null;
		
		String sessionId = request.getSession(false).getId();
		
		String uri = repo.selectRequestCache(sessionId);
		
		if(uri==null)
			return null;
		
		logger.debug("getRequest success");
		
		return new SimpleSavedRequest(uri);		
	}
	
	@Override
	public void removeRequest(HttpServletRequest request, HttpServletResponse response)
	{			
		if(request.getSession(false)==null)
			return;
		
		String sessionId = request.getSession(false).getId();
		repo.deleteRequestCache(sessionId);
		
		logger.debug("removeRequest success");		
	}
	
	@Override
	public void saveRequest(HttpServletRequest request, HttpServletResponse response)
	{		
		if(request.getSession(false)==null)
			return;
		
		String sessionId = request.getSession(false).getId();
		String uri = UrlUtils.buildFullRequestUrl(request);
		
		logger.debug("sessionId:" + sessionId);
		logger.debug("uri:" + uri);
		
		repo.insertRequestCache(sessionId, uri);
		
		logger.debug("saveRequest success: " + uri);
	}
	
	/*
	 * Метод нужен, т.к. после аутентификации меняется id сессии
	 */
	@EventListener
	private void handleHttpSessionIdChangedEvent(HttpSessionIdChangedEvent event)
	{		
		String sessionId = event.getNewSessionId();
		String oldSessionId = event.getOldSessionId();
		repo.updateRequestCache(sessionId, oldSessionId);
	}
	
	
}
