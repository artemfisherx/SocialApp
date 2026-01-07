package com.socialapp.main.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;


/*
 * Предоставляют информацию по текущему аутентифицированному пользователю
 */
@Component
@RequestScope
public class UserResolver {
	
	@Autowired
	private MyUserDetailsService userDetailsService;
	
	@Autowired
	private JdbcClient client;
	
	@Autowired
	private GrantedAuthorityDefaults grantedAuthorityDefaults;
	
	//Получаем id аутентифицированного (текущего) пользователя
	public int getId()
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(AnonymousAuthenticationToken.class.isAssignableFrom(auth.getClass()))
			throw new RuntimeException("Anonymous");
		
		Object principal =  auth.getPrincipal();
			
		MyUserDetails details;	
		
		if(principal instanceof ImmutablePublicKeyCredentialUserEntity user)
		{
			String username = user.getName();
			details = (MyUserDetails)userDetailsService.loadUserByUsername(username);
			return details.getId();			
		}
			
		details = (MyUserDetails)principal;
		return details.getId();			
	}
	
	/*
	 * Проверяем является ли пользователь владельцем указанного канала
	 */
	public boolean isChannelOwner(int channelId)
	{
		int userId = this.getId();
		
		return
		client.sql("SELECT 1 FROM channels WHERE id=:channelId AND owner=:userId")
		.param("channelId", channelId)
		.param("userId", userId)
		.query()
		.listOfRows().size() > 0;
	}
	
	/*
	 * Проверяем имеет ли пользователь роль ADMIN 
	 */
	public boolean isAdmin()
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(AnonymousAuthenticationToken.class.isAssignableFrom(auth.getClass()))
			throw new RuntimeException("Anonymous");
		
		String prefix = grantedAuthorityDefaults.getRolePrefix();
		String admin = prefix + "ADMIN";
		
		return
		auth.getAuthorities().parallelStream()
		.filter(a->a.getAuthority().equals(admin))
		.findAny().isPresent();
	}
	
	/*
	 * Проверяем является ли пользователь подписчиком указанного канала
	 */
	public boolean isSubscriber(int channelId)
	{
		int userId = this.getId();
		
		return
		client.sql("SELECT 1 FROM channel_subscribers WHERE user_id=:userId AND channel=:channelId")
		.param("userId", userId)
		.param("channelId", channelId)
		.query()
		.listOfRows().size()>0;
	}

}
