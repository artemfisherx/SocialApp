package com.socialapp.main.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.socialapp.main.MainRepository;

@Component
public class MyUserDetailsService implements UserDetailsService{
	
	@Autowired
	private MainRepository repo;
	
	@Override
	public MyUserDetails loadUserByUsername(String username)
	{
		MyUserDetails details = repo.getMyUserDetails(username);
		
		if(details==null)
			throw new UsernameNotFoundException(username +" was not found");
		
		return details;
	}
}
