package com.socialapp.main.Entities;

import com.socialapp.main.Enums.PrivacyType;

public class Privacy{
	
	private PrivacyType birthDate;
	private PrivacyType maritalStatus;
	private PrivacyType phone;		
	private PrivacyType about;
	
	public Privacy()
	{
		this.birthDate = PrivacyType.All;
		this.maritalStatus = PrivacyType.All;
		this.phone = PrivacyType.All;
		this.about = PrivacyType.All;			
	}
	
	public Privacy(PrivacyType birthDate, PrivacyType maritalStatus, PrivacyType phone, PrivacyType about)
	{
		this.birthDate = birthDate;
		this.maritalStatus = maritalStatus;
		this.phone = phone;
		this.about = about;			
	}
	
	
	public PrivacyType getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(PrivacyType birthDate) {
		this.birthDate = birthDate;
	}
	public PrivacyType getMaritalStatus() {
		return maritalStatus;
	}
	public void setMaritalStatus(PrivacyType maritalStatus) {
		this.maritalStatus = maritalStatus;
	}
	public PrivacyType getPhone() {
		return phone;
	}
	public void setPhone(PrivacyType phone) {
		this.phone = phone;
	}
	public PrivacyType getAbout() {
		return about;
	}
	public void setAbout(PrivacyType about) {
		this.about = about;
	}

	
}

