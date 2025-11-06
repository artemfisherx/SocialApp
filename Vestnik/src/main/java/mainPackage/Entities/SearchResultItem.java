package mainPackage.Entities;

public class SearchResultItem {
	
	private int num;
	private int id;
	private String title;
	private String text;
	private String table;
	
	public SearchResultItem(int num, int id, String title, String text, String table)
	{
		this.num = num;
		this.id = id;
		this.title = title;
		this.text = text;
		this.table = table;
	}
	
	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
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

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

}
