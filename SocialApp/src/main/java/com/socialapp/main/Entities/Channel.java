package com.socialapp.main.Entities;

import jakarta.validation.constraints.Size;

public class Channel {
	
	private int id;
	
	@Size(min=3, max=20)
	private String title;
	
	@Size(min=10, max=150)
	private String description;
	
	private Profile owner;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Profile getOwner() {
		return owner;
	}
	public void setOwner(Profile owner) {
		this.owner = owner;
	}

}
