package mainPackage.Entities;

import java.time.OffsetDateTime;

public class VcArticle {
	
	private int id;
	private String title;
	private String text;
	private OffsetDateTime dt;	
	private String link;
	
	public VcArticle() {}
	
	public VcArticle(int id, String title, String text, OffsetDateTime dt, String link)
	{
		this(title, text, dt, link);
		this.id = id;		
	}
	
	public VcArticle(String title, String text, OffsetDateTime dt, String link)
	{
		this.title = title;
		this.text = text;
		this.dt = dt;	
		this.link = link;
	}
	
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
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public OffsetDateTime getDt() {
		return dt;
	}
	public void setDt(OffsetDateTime dt) {
		this.dt = dt;
	}
	

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	@Override
	public String toString()
	{
		return this.title + " " + this.link;
	}

}
