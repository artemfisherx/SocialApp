package com.socialapp.main.Entities;

import java.time.OffsetDateTime;

import org.springframework.web.multipart.MultipartFile;

public final class WallMessageIn {
	
	private String text;
	private MultipartFile image;
	private int sender;
	private int receiver;		
	private OffsetDateTime dt;

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public MultipartFile getImage() {
		return image;
	}
	public void setImg(MultipartFile image) {
		this.image = image;
	}
	public int getSender() {
		return sender;
	}
	public void setSender(int sender) {
		this.sender = sender;
	}
	public int getReceiver() {
		return receiver;
	}
	public void setReceiver(int receiver) {
		this.receiver = receiver;
	}
	public OffsetDateTime getDt() {
		return dt;
	}
	public void setDt(OffsetDateTime dt) {
		this.dt = dt;
	}	
}
