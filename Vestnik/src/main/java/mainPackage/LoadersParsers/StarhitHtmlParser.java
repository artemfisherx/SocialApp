package mainPackage.LoadersParsers;


import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import mainPackage.MainLogger;
import mainPackage.AbstractClasses.AbstractNewsParser;
import mainPackage.Entities.News;

public class StarhitHtmlParser extends AbstractNewsParser{	
	
	public StarhitHtmlParser(Queue<String> links, Collection<News> news)
	{
		super(links, news);
	}
	
	@Override
	public Void call() throws Exception
	{
		MainLogger.logger.fine("StarhitHtmlParser start in " + Thread.currentThread().getName());
		
		String link = links.poll();
			
		if(link==null) return null;
		
		try
		{
			Document doc = Jsoup.connect(link).get();
			
			String title = doc.getElementsByTag("h1").first().text();			
		
			String date = doc.getElementsByClass("ds-article-header-date-and-stats__date").first().text();	
			
			Pattern pattern = Pattern.compile("(?<day>.{1,2})\\s*?(?<month>[а-яёА-ЯЁ]+)\\s*?(?<year>\\d{4})\\s*?(?<hour>\\S{2}):(?<mins>\\S{2})");
			Matcher matcher = pattern.matcher(date);
			
			matcher.find();	
			
			String textMonth = matcher.group("month");
			int month=1;
			
			switch(textMonth)
			{
				case "февраля": month=2; break;
				case "марта": month=3; break;
				case "апреля": month=4; break;
				case "мая": month=5; break;
				case "июня": month=6; break;
				case "июля": month=7; break;
				case "августа": month=8; break;
				case "сентября": month=9; break;
				case "октября": month=10; break;
				case "ноября": month=11; break;
				case "декабря": month=12; break;
			}
			
			int day = Integer.valueOf(matcher.group("day").trim());			
			int year = Integer.valueOf(matcher.group("year"));
			int hour = Integer.valueOf(matcher.group("hour"));
			int mins = Integer.valueOf(matcher.group("mins"));
			
			OffsetDateTime dt = OffsetDateTime.of(year, month, day, hour, mins, 0, 0, ZoneOffset.ofHours(3));		
			
			Elements elements = doc.getElementsByClass("ds-block-text text-style-body-1 ds-article-content__block ds-article-content__block_text");
			
			String img = doc.getElementsByClass("ds-block-image ds-article-content__block ds-article-content__block_image").first()
						.getElementsByTag("img").first().attr("src");
			
			String text="";
			for(var element : elements)
			{
				var textItems = element.getElementsByTag("p");		
				for(var item : textItems)
				{
					text+= item.text();
				}					
			}
			
			this.news.add(new News(title, text, dt, img, link));
			
			MainLogger.logger.fine("StarhitHtmlParser success in " + Thread.currentThread().getName());
		
		}
		catch(IOException ex)
		{
			MainLogger.logger.warning("StarhitHtmlParser: не удалось загрузить страницу " + link
					+ " in " + Thread.currentThread().getName());
			
			throw ex;
		}
		
		MainLogger.logger.fine("StarhitHtmlParser finish in " + Thread.currentThread().getName());
		
		return null;
	}
	

}
