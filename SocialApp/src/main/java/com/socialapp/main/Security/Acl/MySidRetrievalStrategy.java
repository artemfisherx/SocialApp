package com.socialapp.main.Security.Acl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.access.hierarchicalroles.NullRoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.domain.SidRetrievalStrategyImpl;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.socialapp.main.Security.MyUserDetails;

/*
 * Используется для преобразования principal в Sid.
 * Предоставляется в AclPermissionEvaluator.setSidRetrievalStrategy(..)
 */
public class MySidRetrievalStrategy extends SidRetrievalStrategyImpl{
	
	private RoleHierarchy roleHierarchy = new NullRoleHierarchy();

	public MySidRetrievalStrategy() {
		
	}
	
	public MySidRetrievalStrategy(RoleHierarchy roleHierarchy)
	{
		this.roleHierarchy = roleHierarchy;
	}
	
	@Override
	public List<Sid> getSids(Authentication authentication) {
		
		Collection<? extends GrantedAuthority> authorities = this.roleHierarchy
			.getReachableGrantedAuthorities(authentication.getAuthorities());
		
		List<Sid> sids = new ArrayList<>(authorities.size() + 1);
		
		if(authentication.getPrincipal().getClass()!=MyUserDetails.class)
			new RuntimeException("Principal is not MyUserDetails class");
		
		MyUserDetails principal = (MyUserDetails)authentication.getPrincipal();
		int id = principal.getId();
		
		sids.add(new MyPrincipalSid(id));
		
		for (GrantedAuthority authority : authorities) {
			sids.add(new GrantedAuthoritySid(authority));
		}
		return sids;
	}
	 

}
