package mainPackage.Entities;

import java.time.OffsetDateTime;

/*
 * Используется для сохранения новостей с сайтов ria.ru, starhit.ru и собственных новостей
 */
public class News {
	
	private int id;
	private String title;
	private String text;
	private OffsetDateTime dt;
	private String img;
	private String link;
	
	public News() {}
	
	public News(int id, String title, String text, OffsetDateTime dt, String img, String link)
	{
		this(title, text, dt, img, link);
		this.id = id;		
	}
	
	public News(String title, String text, OffsetDateTime dt, String img, String link)
	{
		this.title = title;
		this.text = text;
		this.dt = dt;
		this.img = img;
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
	
	@Override
	public String toString()
	{
		return this.title + " " + this.link;
	}

}
