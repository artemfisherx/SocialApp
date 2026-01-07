package com.socialapp.main.Controllers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * Используется в случае, если аутентифицированный пользователь пытается зайти на страницы входа или регистрации.
 * В этом случае будет редирект на страницу его профиля.
 */
@Component
@RequestMapping("${server.error.path:${error.path:/error}}")
public class MyErrorController extends BasicErrorController{	
	
	private Logger logger = LoggerFactory.getLogger(MyErrorController.class);	
	
	public MyErrorController(ErrorAttributes errorAttributes, List<ErrorViewResolver> errorViewResolvers) {		
		super(errorAttributes, new ErrorProperties(), errorViewResolvers);		
	}
	
	/* отключил на время разработки
	@Override
	@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
		
		logger.debug("errorHtml start");
		
		var auth = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
		
		logger.debug("errorHtml auth type:" + auth.getClass());
		
		if(!AnonymousAuthenticationToken.class.isAssignableFrom(auth.getClass()))
		{
			logger.debug("errorHtml: is not anonymous");
			return new ModelAndView("redirect:/profile/");
		}
			
		logger.debug("errorHtml: is anonymous");
		
		return super.errorHtml(request, response);
	}
	*/
	
	@Override
	@RequestMapping
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
		
		ResponseEntity<Map<String, Object>> resp = super.error(request);
		
		logger.debug(resp.getBody().toString());
		
		return resp;
		
	}

}
