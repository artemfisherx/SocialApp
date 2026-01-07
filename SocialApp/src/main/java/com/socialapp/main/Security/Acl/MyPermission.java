package com.socialapp.main.Security.Acl;

import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;

public class MyPermission extends BasePermission{
	
	public static final Permission VIEW_PROFILE = new MyPermission(1<<5, 'V'); // разрешение просматривать профиль	

	protected MyPermission(int mask) {
		super(mask);
	}

	protected MyPermission(int mask, char code) {
		super(mask, code);
	}
}
