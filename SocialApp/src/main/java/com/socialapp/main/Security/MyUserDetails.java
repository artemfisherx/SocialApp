package com.socialapp.main.Security;

import java.util.Collection;
import java.util.Objects;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class MyUserDetails implements UserDetails, CredentialsContainer{
	
	private int id;
	private String username;
	private String password;
	private Collection<? extends GrantedAuthority> authorities;	
	private boolean enabled = true;	
	
	public MyUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities)
	{
		this.username = username;
		this.password = password;
		this.authorities = authorities;					
	}
	
	@Override
	public String getUsername() {
		return username;
	}		

	public String getPassword() {		
		return this.password;		
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public void eraseCredentials()
	{
		this.password = null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}	
	
	@Override
	public boolean equals(Object other)
	{
		if(this==other) return true;
		if(other==null) return false;
		
		if(this.getClass()!=other.getClass())
			return false;
		
		MyUserDetails otherDetails = (MyUserDetails) other;
		
		return this.id==otherDetails.getId();
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.id);
	}
}
