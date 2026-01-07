package com.socialapp.main.Controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutSuccessEventPublishingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.csrf.CsrfLogoutHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.socialapp.main.MainRepository;
import com.socialapp.main.Annotations.LatinNumChars;
import com.socialapp.main.Entities.Privacy;
import com.socialapp.main.Entities.Profile;
import com.socialapp.main.Security.MyJdbcSessionRegistry;
import com.socialapp.main.Security.MySessionInformation;
import com.socialapp.main.Security.MyUserDetails;
import com.socialapp.main.Security.UserResolver;
import com.socialapp.main.Services.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RequestMapping("/settings")
@Controller
@PreAuthorize("hasRole('USER')")
public class SettingsController {
	
	private Logger logger = LoggerFactory.getLogger(SettingsController.class);
	
	@Autowired
	private PublicKeyCredentialUserEntityRepository userEntities;
	
	@Autowired
	private UserCredentialRepository userCredentials;	
	
	@Autowired
	private MyJdbcSessionRegistry sessionRegistry;
	
	@Autowired
	private MainRepository repo;
	
	@Autowired
	private UserDetailsService userDetailsService;	
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserResolver userResolver;		
	
	@Autowired
	private AuthenticationManager authManager;
	
	/**
	 * @param model
	 * @return
	 */
	@GetMapping
	String getSettings(Model model, HttpSession session)
	{			
		
		Object principal =  SecurityContextHolder.getContextHolderStrategy().
							getContext().getAuthentication().getPrincipal();	
		
		MyUserDetails details;	
		String username;
		
		if(principal instanceof ImmutablePublicKeyCredentialUserEntity user)
		{
			username = user.getName();
			details = (MyUserDetails)userDetailsService.loadUserByUsername(username);						
		}
		else
		{
			details = (MyUserDetails)principal;
			username = details.getUsername();
		}
		
		//текущий логин
		model.addAttribute("login", username);
		
		//настройки видимости
		int userId = userResolver.getId();
		Privacy privacy = userService.getPrivacy(userId);
		
		model.addAttribute("privacy", privacy);
		
		//получаем токены Passkeys
		PublicKeyCredentialUserEntity userEntity = userEntities.findByUsername(username);	
		List<CredentialRecord> credentials = new ArrayList<>();
		
		if(userEntity!=null)
			credentials = userCredentials.findByUserId(userEntity.getId());
		
		model.addAttribute("credentials", credentials);
		
		//получаем активные сеансы
		List<MySessionInformation> sessions = sessionRegistry.getAllActiveMySessions(details);					
				
		model.addAttribute("sessions", sessions);
		model.addAttribute("currentSession", session);
		
		//заблокированные пользователи
		List<Profile> blockedUsers = userService.getBlockedUsers(userId);
		model.addAttribute("blockedUsers", blockedUsers);
		
		return "settings";
	}
	
	@ResponseBody
	@PostMapping("/savelogin")
	public void postSaveLogin(@LatinNumChars String login, HttpServletRequest request, HttpServletResponse response)
	{		
		int userId = userResolver.getId();			
		 
		userService.updateLogin(userId, login);	
		
		/*
		 * 
		
		UserDetails principal = userDetailsService.loadUserByUsername(login);		
		
		String password = principal.getPassword();
		System.out.println(password);
		
		ошибка аутетификации
		var preAuthToken = new UsernamePasswordAuthenticationToken(principal, password);
		var postAuthToken = authManager.authenticate(preAuthToken);
		
		var context = SecurityContextHolder.getContextHolderStrategy().getContext();
		context.setAuthentication(postAuthToken);
		SecurityContextHolder.getContextHolderStrategy().setContext(context);
		
		var repo = new HttpSessionSecurityContextRepository();
		repo.saveContext(context, request, response);		
		*/
		
	}	
	
	@PostMapping("/saveprivacy")
	public String postSavePrivacy(Privacy privacy)
	{	
		int userId = userResolver.getId();
		userService.updatePrivacy(userId, privacy);		
		
		return "redirect:/settings/";
	}
	
	@PostMapping("/unblockuser")
	@ResponseBody
	public void postUnblockUser(int userId)
	{
		userService.unblockUser(userId);
	}
	
	@ResponseBody
	@PostMapping("/removesession")
	public void postRemoveSession(String sessionId, HttpServletRequest request, HttpServletResponse response)
	{	
		String currentSessionId = request.getSession(false).getId();
		
		if(sessionId.equals(currentSessionId))
		{
			try
			{
				Authentication auth = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
				
				logout(request, response, auth); 
			}
			catch(ServletException|IOException ex)
			{
				logger.debug(ex.getMessage());
				return;
			}			
		}		
		
		sessionRegistry.removeSessionInformation(sessionId);			
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
	}
	
	

}
