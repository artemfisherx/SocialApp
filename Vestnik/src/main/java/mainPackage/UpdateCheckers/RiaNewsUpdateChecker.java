package mainPackage.UpdateCheckers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import mainPackage.MainLogger;

/*
 * Выполняет проверку наличия обновлений новостей от РИА Новости
 */

@Component
public class RiaNewsUpdateChecker {
	
	private final String URL="https://ria.ru/export/rss2/archive/index.xml";
	
	public boolean hasUpdate(OffsetDateTime lastUpdateDt)
	{
		MainLogger.logger.fine("RiaNewsUpdateChecker hasUpdate() start");
		
		try
		{
			HttpRequest request = HttpRequest.newBuilder(URI.create(URL)).build();
			
			HttpClient client = HttpClient.newHttpClient();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			
			if(response==null)
			{
				MainLogger.logger.fine("RiaNewsUpdateChecker hasUpdate() response is null");
				return false;
			}
				
			Pattern pattern = Pattern.compile("<item>.*?<pubDate>(.*?)</pubDate>", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(response.body());
				
			List<OffsetDateTime> dates = new ArrayList<>();
			
			while(matcher.find())
			{
				String d = matcher.group(1);
				OffsetDateTime dt = OffsetDateTime.parse(d, 
						DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss x", Locale.US));
				
				dates.add(dt);			
			}
			
			dates.sort((d1, d2)->d2.compareTo(d1));
			OffsetDateTime lastNewsDt = dates.get(0);	
							
			if(lastNewsDt.compareTo(lastUpdateDt)>0)
				return true;
		}
		catch(InterruptedException|IOException ex)
		{
			MainLogger.logger.warning("RiaNewsUpdateChecker hasUpdate() exception: " + ex.getMessage());
		}		
		
		return false;		
		
	}

}
