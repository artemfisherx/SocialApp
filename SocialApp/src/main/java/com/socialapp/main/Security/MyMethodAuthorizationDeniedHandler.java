package com.socialapp.main.Security;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.method.MethodAuthorizationDeniedHandler;
import org.springframework.security.authorization.method.MethodInvocationResult;
import org.springframework.stereotype.Component;

import com.socialapp.main.Entities.Privacy;
import com.socialapp.main.Entities.Profile;
import com.socialapp.main.Enums.PrivacyType;
import com.socialapp.main.Services.UserService;

@Component
public class MyMethodAuthorizationDeniedHandler implements MethodAuthorizationDeniedHandler{
	
	@Autowired
	private UserResolver userResolver;
	
	@Autowired
	private UserService userService;
	
	@Override
	public Object handleDeniedInvocation(MethodInvocation methodInvocation, AuthorizationResult authorizationResult)
	{	
		return null;
	}
	
	@Override
	public Object handleDeniedInvocationResult(MethodInvocationResult methodInvocationResult, AuthorizationResult authorizationResult)
	{
		if(!methodInvocationResult.getResult().getClass().isAssignableFrom(Profile.class))
			return null;
				
		int currentUserId = userResolver.getId();
		Profile profile = (Profile)methodInvocationResult.getResult();		
		
		//проверяем настройки видимости для просматриваемого профиля		
		Privacy privacy = userService.getPrivacy(profile.getId());
		
		//выясняем являются ли они друзьями
		boolean isFriend = userService.isFriend(currentUserId, profile.getId());
		
		//если видимость только для друзей, а пользователь не является другом для просматриваемого профиля
		
		if(privacy.getBirthDate().equals(PrivacyType.Friends)&&!isFriend)
			profile.setBirthDate(null);
		
		if(privacy.getMaritalStatus().equals(PrivacyType.Friends)&&!isFriend)
			profile.setMaritalStatus(null);
		
		if(privacy.getPhone().equals(PrivacyType.Friends)&&!isFriend)
			profile.setPhone(null);
		
		if(privacy.getAbout().equals(PrivacyType.Friends)&&!isFriend)
			profile.setAbout(null);
		
		return profile;
		
	}

}
