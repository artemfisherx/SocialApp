package com.socialapp.main.Security.Acl;

import java.awt.datatransfer.StringSelection;

import javax.sql.DataSource;

import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.util.Assert;

import com.socialapp.main.Security.MyUserDetails;

/*
 * Т.к. используем свои UserDetails и Sid, переопределяем методы, в которых они используются
 */

public class MyJdbcMutableAclService extends JdbcMutableAclService{
	
	private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
			.getContextHolderStrategy();

	public MyJdbcMutableAclService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
		super(dataSource, lookupStrategy, aclCache);		
	}
	
	@Override
	public MutableAcl createAcl(ObjectIdentity objectIdentity) throws AlreadyExistsException {
		Assert.notNull(objectIdentity, "Object Identity required");

		// Check this object identity hasn't already been persisted
		if (retrieveObjectIdentityPrimaryKey(objectIdentity) != null) {
			throw new AlreadyExistsException("Object identity '" + objectIdentity + "' already exists");
		}

		// Need to retrieve the current principal, in order to know who "owns" this ACL
		// (can be changed later on)
		Authentication auth = this.securityContextHolderStrategy.getContext().getAuthentication();
		
		if(!MyUserDetails.class.isAssignableFrom(auth.getPrincipal().getClass()))
			throw new RuntimeException("Principal is not MyUserDetails class");
		
		MyUserDetails principal = (MyUserDetails)auth.getPrincipal();
		int id = principal.getId();
		
		MyPrincipalSid sid = new MyPrincipalSid(id);

		// Create the acl_object_identity row
		createObjectIdentity(objectIdentity, sid);

		// Retrieve the ACL via superclass (ensures cache registration, proper retrieval
		// etc)
		Acl acl = readAclById(objectIdentity);
		Assert.isInstanceOf(MutableAcl.class, acl, "MutableAcl should be been returned");

		return (MutableAcl) acl;
	}
	
	@Override
	protected Long createOrRetrieveSidPrimaryKey(Sid sid, boolean allowCreate) {
		Assert.notNull(sid, "Sid required");
		if (sid instanceof MyPrincipalSid) {
			int id = ((MyPrincipalSid) sid).getUserId();
			String sidName = String.valueOf(id);
			return createOrRetrieveSidPrimaryKey(sidName, true, allowCreate);
		}
		if (sid instanceof GrantedAuthoritySid) {
			String sidName = ((GrantedAuthoritySid) sid).getGrantedAuthority();
			return createOrRetrieveSidPrimaryKey(sidName, false, allowCreate);
		}
		throw new IllegalArgumentException("Unsupported implementation of Sid");
	}
	
	@Override
	public void setSecurityContextHolderStrategy(SecurityContextHolderStrategy securityContextHolderStrategy) {
		Assert.notNull(securityContextHolderStrategy, "securityContextHolderStrategy cannot be null");
		this.securityContextHolderStrategy = securityContextHolderStrategy;
	}

}
