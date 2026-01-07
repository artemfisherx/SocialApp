package com.socialapp.main.Security.Acl;

import java.util.Objects;

import org.springframework.security.acls.model.Sid;

public class MyPrincipalSid implements Sid{
	
	private int userId;
	
	public MyPrincipalSid(int userId)
	{
		this.userId = userId;
	}
	
	public int getUserId()
	{
		return this.userId;
	}
	
	
	@Override
	public boolean equals(Object other)
	{
		if(this==other) return true;
		if(other==null) return false;
		if(this.getClass()!=other.getClass()) return false;
				
		MyPrincipalSid otherObj = (MyPrincipalSid) other;
		
		return this.userId==otherObj.userId;
	}
	
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.userId);
	}	
}
