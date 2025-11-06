package mainPackage.AbstractClasses;

import java.util.Collection;
import java.util.Queue;

import mainPackage.Entities.News;
import mainPackage.Interfaces.IParser;

/*
 * Этот класс нужен для того, чтобы реализации парсеров гарантировано имели одинаковый базовый набор полей.
 * Классы, являющиеся реализациями парсеров, должны расширять этот класс, а не реализовывать интерфейс mainPackage.Interfaces.IParser.
 */
public abstract class AbstractNewsParser implements IParser{
	
	protected Queue<String> links;
	protected Collection<News> news;
	
	public AbstractNewsParser(Queue<String> links, Collection<News> news)
	{
		this.links = links;
		this.news = news;
	}
	
	@Override
	public abstract Void call() throws Exception;	

}
