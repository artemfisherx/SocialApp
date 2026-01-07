package com.socialapp.main.Security.Acl;

import javax.sql.DataSource;

import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.acls.model.Sid;

/*
 * Т.к. используется свой класс, реализующий Sid, переопределяем создание экземпляра Sid
 */

public class MyBasicLookupStrategy extends BasicLookupStrategy{

	public MyBasicLookupStrategy(DataSource dataSource, AclCache aclCache,
			AclAuthorizationStrategy aclAuthorizationStrategy, AuditLogger auditLogger) {
		this(dataSource, aclCache, aclAuthorizationStrategy, new DefaultPermissionGrantingStrategy(auditLogger));
	}
	
	public MyBasicLookupStrategy(DataSource dataSource, AclCache aclCache,
			AclAuthorizationStrategy aclAuthorizationStrategy, PermissionGrantingStrategy grantingStrategy) {
		super(dataSource, aclCache, aclAuthorizationStrategy, grantingStrategy);
		
	}
	
	@Override
	protected Sid createSid(boolean isPrincipal, String sid) {
		if (isPrincipal) {
			{
				int id = Integer.valueOf(sid);
				return new MyPrincipalSid(id);
			}			
			
		}
		return new GrantedAuthoritySid(sid);
	}


}
