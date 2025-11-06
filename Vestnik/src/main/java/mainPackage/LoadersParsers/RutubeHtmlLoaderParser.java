package mainPackage.LoadersParsers;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import mainPackage.MainLogger;
import mainPackage.Entities.Video;
import mainPackage.Interfaces.IParser;

public class RutubeHtmlLoaderParser implements IParser{

	private static final String URL = "https://rutube.ru/feeds/popular";
	private static final String SITE = "https://rutube.ru";
	private Collection<Video> videos;
	
	public RutubeHtmlLoaderParser(Collection<Video> videos)
	{
		this.videos = videos;
	}
	
	
	public Void call() throws Exception
	{
		try
		{		
			MainLogger.logger.fine("RutubeHtmlLoaderParser start in " + Thread.currentThread().getName());
			
			Document doc = Jsoup.connect(URL).get();
			Element videoGroup = doc.getElementsByClass("wdp-showcase-module__wdp-showcase wdp-showcase-module__wdp-showcase_full-width")
									.get(1);
			Elements elements = videoGroup.getElementsByClass("wdp-grid-module__item");				
			
			for(Element element:elements)
			{	
				String link = SITE + element.getElementsByClass("wdp-link-module__link wdp-card-poster-module__posterWrapper")
						.first().attr("href");	
				
				String img = element.getElementsByClass("wdp-card-poster-module__image wdp-card-poster-module__imageBg")
								.first().attr("src");
				img = img.replaceAll("\\?.*$", "");	
				
				String name = element.getElementsByClass("wdp-card-poster-module__image wdp-card-poster-module__imageBg")
								.first().attr("alt");
				
				String dt = element.getElementsByClass("wdp-poster-badge-module__poster-badge wdp-card-video-options-module__duration")
								.first().text().trim();								
				
				Pattern pattern = Pattern.compile("(?<h>\\d{0,2}):{0,1}(?<m>\\d{2}):(?<s>\\d{2})$");
				Matcher matcher = pattern.matcher(dt);
				
				if(matcher.find())
				{
					long hours = Long.parseLong(matcher.group("h").length()>0 ? matcher.group("h") : "0");
					long mins = Long.parseLong(matcher.group("m").length()>0 ? matcher.group("m") : "0");
					long seconds = Long.parseLong(matcher.group("s").length()>0 ? matcher.group("s") : "0");
														
					LocalTime duration = LocalTime.MIN.plusHours(hours).plusMinutes(mins).plusSeconds(seconds);	
					
					videos.add(new Video(name, duration, img, link));
					
					MainLogger.logger.fine("RutubeHtmlLoaderParser success video name: " + name);
				}		
			}
			
			MainLogger.logger.fine("RutubeHtmlLoaderParser success in " + Thread.currentThread().getName());
			
		}
		catch(IOException ex)
		{
			
			MainLogger.logger.warning("RutubeHtmlLoaderParser: не удалось загрузить страницу " + URL
					+ " in " + Thread.currentThread().getName());
			
			throw ex;			
		}	
		
		MainLogger.logger.fine("RutubeHtmlLoaderParser finish in " + Thread.currentThread().getName());
		
		return null;
	}
	

}
