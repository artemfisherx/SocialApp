package com.socialapp.main.Security;

import java.util.Date;

import org.springframework.security.core.session.SessionInformation;

public class MySessionInformation extends SessionInformation{
	
	private int id;
	private String ip;
	
	public MySessionInformation(Object principal, String sessionId, Date lastRequest, int id, String ip)
	{
		super(principal, sessionId, lastRequest);
		this.id = id;
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}	 

}
