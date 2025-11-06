package mainPackage.LoadersParsers;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mainPackage.MainLogger;
import mainPackage.Interfaces.ILoader;

@Service("starhitHtmlLoader")
public class StarhitHtmlLoader implements ILoader{	
	
	private static final String URL="https://www.starhit.ru";		
	
	@Override
	public Queue<String> load() throws Exception
	{
		try
		{
			MainLogger.logger.fine("StarhitHtmlLoader start in" + Thread.currentThread().getName());
			
			ConcurrentLinkedQueue<String> links = new ConcurrentLinkedQueue<>();
			Document doc = Jsoup.connect(URL).get();
			Elements elements = doc.getElementsByClass("announce-inline-tile chronology-lenta-news-item _desktop _has-top-line");
			
			
			for(var element : elements)
			{
				String link = URL+element.attr("href");
				links.add(link);
				System.out.println(link);
			}
			
			MainLogger.logger.fine("StarhitHtmlLoader success in " + Thread.currentThread().getName());
			
			return links;
		}
		catch(IOException ex)
		{
			MainLogger.logger.warning("StarhitHtmlLoader: Не удалось загрузить страницу " + URL 
					+ " in " + Thread.currentThread().getName());
			throw ex;
		}
		
	}

}
