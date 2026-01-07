package com.socialapp.main.Entities;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.socialapp.main.Annotations.FileSize;
import com.socialapp.main.Annotations.LatinNumChars;
import com.socialapp.main.Enums.MaritalStatus;
import com.socialapp.main.Security.MyUserDetails;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

/*
 * Используется для сохранения данных регистрации
 */

public final class RegForm {
	
	@NotBlank(groups=RegisterStep1.class)
	@Size(min=3, max=20, groups=RegisterStep1.class)
	@LatinNumChars(groups=RegisterStep1.class)
	private String login;
		
	@NotBlank(groups=RegisterStep1.class)
	@Size(min=3, max=20, groups=RegisterStep1.class)
	private String password;
	
	@NotBlank(groups=RegisterStep1.class)
	@Size(min=3, max=20, groups=RegisterStep1.class)
	private String name;
	
	@NotBlank(groups=RegisterStep1.class)
	@Size(min=3, max=20, groups=RegisterStep1.class)
	private String surname;
		
	@PastOrPresent(groups=RegisterStep1.class)
	private LocalDate birthDate;
	
	@NotNull(groups=RegisterStep1.class)
	private MaritalStatus maritalStatus;
		
	@NotBlank(groups=RegisterStep2.class)
	@Size(min=15, max=15, groups=RegisterStep2.class)
	private String phone;
		
	@NotBlank(groups=RegisterStep2.class)
	@Size(min=3, max=20, groups=RegisterStep2.class)
	private String country;
		
	@NotBlank(groups=RegisterStep2.class)
	@Size(min=3, max=20, groups=RegisterStep2.class)
	private String city;
	
	@NotBlank(groups=RegisterStep2.class)
	@Size(min=10, max=150, groups=RegisterStep2.class)
	private String about;
	
	@NotNull(groups=RegisterStep2.class)
	@FileSize(groups=RegisterStep2.class)
	private MultipartFile image;

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public MultipartFile getImage() {
		return image;
	}

	public void setImage(MultipartFile image) {
		this.image = image;
	}
	
	@Override
	public String toString()
	{
		return this.login + " " + this.password + " " + this.name + " " + this.surname + " " +
				this.birthDate + " " + this.maritalStatus + " " + this.phone + " " + this.country + " " +
				this.city + " " + this.about + " " + this.image.getSize();
	}
	
	public interface RegisterStep1{}
	public interface RegisterStep2{}	
	
}
