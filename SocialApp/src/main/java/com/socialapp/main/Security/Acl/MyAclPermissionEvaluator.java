package com.socialapp.main.Security.Acl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.domain.ObjectIdentityRetrievalStrategyImpl;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.domain.SidRetrievalStrategyImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.core.Authentication;

/*
 * Используется чтобы запретить просмотр профиля тем, кто в черном списке
 */
public class MyAclPermissionEvaluator extends AclPermissionEvaluator{
	
	private Logger logger = LoggerFactory.getLogger(MyAclPermissionEvaluator.class);
	
	private ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy = new ObjectIdentityRetrievalStrategyImpl();

	private final AclService aclService;
	
	private PermissionFactory permissionFactory = new DefaultPermissionFactory();
	
	private SidRetrievalStrategy sidRetrievalStrategy = new SidRetrievalStrategyImpl();
	
	public MyAclPermissionEvaluator(AclService aclService) {
		
		super(aclService);		
		this.aclService = aclService;
	}
	
	@Override
	public boolean hasPermission(Authentication authentication, Object domainObject, Object permission) {
		if (domainObject == null) {
			return false;
		}
		
		ObjectIdentity oid = this.objectIdentityRetrievalStrategy.getObjectIdentity(domainObject);
		
		List<Sid> sids = this.sidRetrievalStrategy.getSids(authentication);
		List<Permission> requiredPermission = this.resolvePermission(permission);
		this.logger.debug("Checking permission '" + permission + "' for object '" + oid + "'");
		try {
			// Lookup only ACLs for SIDs we're interested in
			Acl acl = this.aclService.readAclById(oid, sids);
			if (acl.isGranted(requiredPermission, sids, false)) {
				this.logger.debug("Access is granted");
				return true;
			}
			this.logger.debug("Returning false - ACLs returned, but insufficient permissions for this principal");
		}
		catch (NotFoundException nfe) {
			//т.к. в базе хранятся только sid, для которых запрещено просматривать данный профиль
			//тогда если в базе нет соответствующей записи для sid, значит разрешено
			this.logger.debug("Returning true - no ACLs apply for this principal");
			return true;
		}
		return false;
			
	}
		
	List<Permission> resolvePermission(Object permission) {
		if (permission instanceof Integer) {
			return Arrays.asList(this.permissionFactory.buildFromMask((Integer) permission));
		}
		if (permission instanceof Permission) {
			return Arrays.asList((Permission) permission);
		}
		if (permission instanceof Permission[]) {
			return Arrays.asList((Permission[]) permission);
		}
		if (permission instanceof String permString) {
			Permission p = buildPermission(permString);
			if (p != null) {
				return Arrays.asList(p);
			}
		}
		throw new IllegalArgumentException("Unsupported permission: " + permission);
	}
	
	private Permission buildPermission(String permString) {
		try {
			return this.permissionFactory.buildFromName(permString);
		}
		catch (IllegalArgumentException notfound) {
			return this.permissionFactory.buildFromName(permString.toUpperCase(Locale.ENGLISH));
		}
	}
	
	@Override
	public void setObjectIdentityRetrievalStrategy(ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy) {	
		super.setObjectIdentityRetrievalStrategy(objectIdentityRetrievalStrategy);
		this.objectIdentityRetrievalStrategy = objectIdentityRetrievalStrategy;
	}
	
	public void setSidRetrievalStrategy(SidRetrievalStrategy sidRetrievalStrategy) {
		super.setSidRetrievalStrategy(sidRetrievalStrategy);
		this.sidRetrievalStrategy = sidRetrievalStrategy;
	}

	public void setPermissionFactory(PermissionFactory permissionFactory) {
		super.setPermissionFactory(permissionFactory);
		this.permissionFactory = permissionFactory;
	}

}
