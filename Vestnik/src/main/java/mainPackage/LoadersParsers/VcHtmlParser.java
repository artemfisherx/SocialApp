package mainPackage.LoadersParsers;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import mainPackage.MainLogger;
import mainPackage.Entities.News;
import mainPackage.Entities.VcArticle;
import mainPackage.Interfaces.IParser;

public class VcHtmlParser implements IParser{
	
	private Queue<String> links;
	private Collection<VcArticle> vcArticles;
	
	public VcHtmlParser(Queue<String> links, Collection<VcArticle> vcArticles)
	{
		this.links = links;
		this.vcArticles = vcArticles;
	}
	
	
	public Void call() throws Exception
	{
		MainLogger.logger.fine("VcHtmlParser start in " + Thread.currentThread().getName());
		
		String link = links.poll();
			
		if(link==null) return null;
		
		try 
		{
			Document doc = Jsoup.connect(link).get();	
			
			String date = doc.getElementsByTag("time").attr("datetime");
			String offset = "+0300"; // смещение относительно Московского часового пояса
			date = date.replace("Z", "") + offset;				
			
			OffsetDateTime dt = OffsetDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxxx"));		
						
			String title = doc.getElementsByTag("h1").first().text();	
			
			
			Elements elements = doc.getElementsByClass("block-wrapper__content");
			
			String text="";
			
			for(var element:elements)
			{
				text+=element.html();
			}								
			
			this.vcArticles.add(new VcArticle(title, text, dt, link));		
			
			MainLogger.logger.fine("VcHtmlParser success in " + Thread.currentThread().getName());
			
		}
		catch(IOException ex)
		{
			MainLogger.logger.warning("VcHtmlParser: не удалось загрузить страницу " + link
					+ " in " + Thread.currentThread().getName());
			throw ex;
		}
		
		MainLogger.logger.fine("VcHtmlParser finish in " + Thread.currentThread().getName());
	
		return null;
	}

}
