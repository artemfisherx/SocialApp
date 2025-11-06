package mainPackage.LoadersParsers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import mainPackage.MainLogger;
import mainPackage.Entities.Horoscope;
import mainPackage.Enums.HoroscopeSign;
import mainPackage.Interfaces.IParser;

public class HoroscopeHtmlLoaderParser implements IParser{
	
	private static Queue<String> links;
	static
	{
		links = new ConcurrentLinkedQueue<>();
		links.add("https://1001goroskop.ru/?znak=aries");
		links.add("https://1001goroskop.ru/?znak=taurus");
		links.add("https://1001goroskop.ru/?znak=gemini");
		links.add("https://1001goroskop.ru/?znak=cancer");
		links.add("https://1001goroskop.ru/?znak=leo");
		links.add("https://1001goroskop.ru/?znak=virgo");
		links.add("https://1001goroskop.ru/?znak=libra");
		links.add("https://1001goroskop.ru/?znak=scorpio");
		links.add("https://1001goroskop.ru/?znak=sagittarius");
		links.add("https://1001goroskop.ru/?znak=capricorn");
		links.add("https://1001goroskop.ru/?znak=aquarius");
		links.add("https://1001goroskop.ru/?znak=pisces");		
	}
	
	Collection<Horoscope> horoscopes;
	
	public HoroscopeHtmlLoaderParser(Collection<Horoscope> horoscopes)
	{
		this.horoscopes = horoscopes;
	}
	
	public Void call() throws Exception
	{
		MainLogger.logger.fine("HoroscopeHtmlLoaderParser start in " + Thread.currentThread().getName());
		
		String link = links.poll();
		if(link == null) return null;
		
		try
		{
			Document doc = Jsoup.connect(link).get();
			String text = doc.selectFirst("#eje_text > tbody div > p").text();
			
			Pattern pattern = Pattern.compile("znak=(?<sign>.+)$");
			Matcher matcher = pattern.matcher(link);
			String sign = "aries";
			
			if(matcher.find())
				sign=matcher.group("sign");					
			
			
			this.horoscopes.add(new Horoscope(HoroscopeSign.valueOf(sign), LocalDate.now(), text));
			
			MainLogger.logger.fine("HoroscopeHtmlLoaderParser success in " + Thread.currentThread().getName());
		}
		catch(IOException ex)
		{
			MainLogger.logger.warning("HoroscopeHtmlLoaderParser: не удалось загрузить страницу " + link
					+ " in " + Thread.currentThread().getName());
			
			throw ex;
		}
		
		MainLogger.logger.fine("HoroscopeHtmlLoaderParser finish in " + Thread.currentThread().getName());
		
		return null;
		
	}

}
