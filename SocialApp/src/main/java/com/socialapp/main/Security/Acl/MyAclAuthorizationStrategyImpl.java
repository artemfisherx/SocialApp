package com.socialapp.main.Security.Acl;

import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.socialapp.main.Security.MyUserDetails;

public class MyAclAuthorizationStrategyImpl extends AclAuthorizationStrategyImpl{

	public MyAclAuthorizationStrategyImpl(GrantedAuthority... auths) {
		super(auths);
	}

	@Override
	protected Sid createCurrentUser(Authentication authentication) {
		
		if(!MyUserDetails.class.isAssignableFrom(authentication.getPrincipal().getClass()))
			throw new RuntimeException("Principal is not MyUserDetails class");
		
		MyUserDetails principal = (MyUserDetails)authentication.getPrincipal();
		int id = principal.getId();
		
		return new MyPrincipalSid(id);
	}

}
