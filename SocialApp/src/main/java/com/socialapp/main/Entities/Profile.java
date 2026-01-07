package com.socialapp.main.Entities;

import java.time.LocalDate;

import com.socialapp.main.Enums.MaritalStatus;

public class Profile {
	
	private int id;
	private String name;
	private String surname;
	private LocalDate birthDate;
	private MaritalStatus maritalStatus;
	private String phone;
	private String country;
	private String city;
	private String about;
	private String imagePath;
	
	public Profile(int id, String name, String surname, LocalDate birthDate, MaritalStatus maritalStatus,
			String phone, String country, String city, String about, String imagePath)
	{
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.birthDate = birthDate;
		this.maritalStatus = maritalStatus;
		this.phone = phone;
		this.country = country;
		this.city = city;
		this.about = about;
		this.imagePath = imagePath;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
		
	public LocalDate getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}
	public MaritalStatus getMaritalStatus() {
		return maritalStatus;
	}
	public void setMaritalStatus(MaritalStatus maritalStatus) {
		this.maritalStatus = maritalStatus;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getAbout() {
		return about;
	}
	public void setAbout(String about) {
		this.about = about;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}	

}
