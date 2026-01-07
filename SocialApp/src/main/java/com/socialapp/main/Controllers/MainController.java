package com.socialapp.main.Controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@Controller
@PreAuthorize("authentication.getName().equals('anonymousUser')")
public class MainController {
	
	@GetMapping("/")
	public String getIndex(@CurrentSecurityContext SecurityContext securityContext)
	{		
		//Authentication auth = securityContext.getAuthentication();
		
		//if(auth.getClass()!=AnonymousAuthenticationToken.class&&auth.isAuthenticated())
		//	return "redirect:/profile/";
		
		return "index";
	}

}
