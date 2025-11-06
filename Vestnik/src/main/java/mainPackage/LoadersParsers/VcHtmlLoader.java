package mainPackage.LoadersParsers;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import mainPackage.MainLogger;
import mainPackage.Interfaces.ILoader;

@Service("vcHtmlLoader")
public class VcHtmlLoader implements ILoader{
	
	private static final String URL = "https://vc.ru";
	
	public Queue<String> load() throws Exception
	{
		try
		{
			MainLogger.logger.fine("VcHtmlLoader start in" + Thread.currentThread().getName());
			
			ConcurrentLinkedQueue<String> links = new ConcurrentLinkedQueue<>();
			Document doc = Jsoup.connect(URL).get();
			Elements elements = doc.getElementsByClass("link-button link-button--default");
			
			for(var element:elements)			
				links.add(URL+element.attr("href"));
			
			MainLogger.logger.fine("VcHtmlLoader success in " + Thread.currentThread().getName());
			
			return links;
			
		}
		catch(IOException ex)
		{
			MainLogger.logger.warning("VcHtmlLoader: Не удалось загрузить страницу " + URL 
					+ " in " + Thread.currentThread().getName());
			throw ex;
		}
	}

}
