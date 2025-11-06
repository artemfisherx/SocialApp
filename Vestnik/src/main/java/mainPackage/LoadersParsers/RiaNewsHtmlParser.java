package mainPackage.LoadersParsers;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import mainPackage.MainLogger;
import mainPackage.AbstractClasses.AbstractNewsParser;
import mainPackage.Entities.News;

public class RiaNewsHtmlParser extends AbstractNewsParser{	
	
	public RiaNewsHtmlParser(Queue<String> links, Collection<News> news)
	{
		super(links, news);
	}
	
	@Override
	public Void call() throws Exception
	{
		MainLogger.logger.fine("RiaNewsHtmlParser start in " + Thread.currentThread().getName());
		
		String link = links.poll();		
		if(link==null) return null;	
		
		try
		{				
			Document doc = Jsoup.connect(link).get();
			
			String date = doc.getElementsByClass("article__info-date").first().firstElementChild().text();
			String zoneOffset=" +0300";
			date+=zoneOffset;
			OffsetDateTime dt = OffsetDateTime.parse(date, DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy x"));
			
			String title = doc.getElementsByClass("article__title").first().text();
			String img = doc.getElementsByAttribute("data-photoview-src").attr("data-photoview-src");
			
			Elements elements = doc.getElementsByClass("article__text");
			String text = "";
			for(Element el:elements)
			{
				text+=el.text();
			}
			
			Pattern pattern = Pattern.compile("<.*?>");
			Matcher matcher = pattern.matcher(text);
			String clearText = matcher.replaceAll("");
			
			this.news.add(new News(title, clearText, dt, img, link));	
			
			MainLogger.logger.fine("RiaNewsHtmlParser success in " + Thread.currentThread().getName());
		
		}
		catch(IOException ex)
		{			
			MainLogger.logger.warning("RiaNewsHtmlParser: не удалось загрузить страницу " + link
					+ " in " + Thread.currentThread().getName());
			
			throw ex;
		}
		
		MainLogger.logger.fine("RiaNewsHtmlParser finish in " + Thread.currentThread().getName());
		
		return null;
		
	}

}
