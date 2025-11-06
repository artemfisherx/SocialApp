package mainPackage.Entities;

import java.util.Collection;

/*
 * @param count - общее количество строк, которое выдал поисковый запрос
 */
public class SearchResult {
	
	private int count;
	private Collection<SearchResultItem> items;
	
	public SearchResult(int count, Collection<SearchResultItem> items)
	{
		this.setCount(count);
		this.setItems(items);
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Collection<SearchResultItem> getItems() {
		return items;
	}

	public void setItems(Collection<SearchResultItem> items) {
		this.items = items;
	}

}
