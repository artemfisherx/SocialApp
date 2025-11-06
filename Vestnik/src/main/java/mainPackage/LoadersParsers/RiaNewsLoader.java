package mainPackage.LoadersParsers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import mainPackage.MainLogger;
import mainPackage.Interfaces.ILoader;

@Service("riaNewsLoader")
public class RiaNewsLoader implements ILoader{
	
	private final static String URL="https://ria.ru/export/rss2/archive/index.xml";	
		
	public Queue<String> load() throws Exception
	{
		try
		{
			MainLogger.logger.fine("RiaNewsLoader load() start");
			
			ConcurrentLinkedQueue<String> links = new ConcurrentLinkedQueue<>();
			
			//переделать чтобы работал с Jsoup
			
			//ExecutorService executor = Executors.newCachedThreadPool();		
			HttpRequest request = HttpRequest.newBuilder(URI.create(URL)).build();
			
			HttpClient client = HttpClient.newHttpClient();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
				
			Pattern pattern = Pattern.compile("<item>.*?<link>(.*?)</link>", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(response.body());
				
			String link;
			while(matcher.find())
			{
				link = matcher.group(1);
				links.offer(link);			
				System.out.println(link);			
			}
			
			MainLogger.logger.fine("RiaNewsLoader load() finish");
			
			return links;
		}
		catch(InterruptedException ex)
		{
			MainLogger.logger.warning("RiaNewsLoader load() exception: " + ex.getMessage());
			throw ex;
		}
			
	}									
		
}


