package mainPackage.LoadersParsers;

import java.io.IOException;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import mainPackage.MainLogger;
import mainPackage.Interfaces.IParser;

public class WikipediaHtmlLoaderParser implements IParser{
	
	private final String URL = "https://ru.wikipedia.org/";
	private StringBuilder builder;
	
	public WikipediaHtmlLoaderParser(StringBuilder builder)
	{
		this.builder = builder;
	}
	
	@Override
	public Void call()
	{				
		try
		{
			Document doc = Jsoup.connect(URL).get();
			Element eventsBox = doc.getElementById("main-itd");
			String eventsList = eventsBox.getElementsByTag("ul").first().toString();
			
			String result = Pattern.compile("</?a.*?>").matcher(eventsList).replaceAll("");
			result = Pattern.compile("(на илл.)").matcher(result).replaceAll("");			
			
			builder.append(result);					
		}
		catch(IOException ex)
		{
			MainLogger.logger.warning("Не удалось загрузить сайт " + URL);
		}
		
		return null;
	}

}
