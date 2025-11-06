package mainPackage.Entities;

import java.time.Duration;
import java.time.LocalTime;

public class Video {
	
	private int id;
	private String name;
	private LocalTime duration;
	private String img;
	private String link;
	
	public Video(int id, String name, LocalTime duration, String img, String link)
	{
		this(name, duration, img, link);
		this.id = id;		
	}
	
	public Video(String name, LocalTime duration, String img, String link)
	{
		this.name = name;
		this.duration = duration;
		this.img = img;
		this.link = link;
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
	public LocalTime getDuration() {
		return duration;
	}
	public void setDuration(LocalTime duration) {
		this.duration = duration;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}	

}
