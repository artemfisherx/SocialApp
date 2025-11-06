package mainPackage.Services;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import mainPackage.MainLogger;
import mainPackage.Entities.Horoscope;
import mainPackage.Entities.News;
import mainPackage.Entities.VcArticle;
import mainPackage.Entities.Video;
import mainPackage.Interfaces.ILoader;
import mainPackage.Interfaces.IParser;
import mainPackage.Interfaces.IRepository;
import mainPackage.Interfaces.IStarter;
import mainPackage.LoadersParsers.HoroscopeHtmlLoaderParser;
import mainPackage.LoadersParsers.RiaNewsHtmlParser;
import mainPackage.LoadersParsers.RutubeHtmlLoaderParser;
import mainPackage.LoadersParsers.StarhitHtmlParser;
import mainPackage.LoadersParsers.VcHtmlParser;
import mainPackage.LoadersParsers.WikipediaHtmlLoaderParser;
import mainPackage.Proxying.ParserInvocationHandler;

@Service
public class NewsService {
	
	@Autowired
	@Qualifier("mainRepository")
	private IRepository repo;
	
	@Autowired
	@Qualifier("riaNewsLoader")
	private ILoader riaNewsLoader;
	
	@Autowired
	@Qualifier("starhitHtmlLoader")
	private ILoader starhitHtmlLoader;
	
	@Autowired
	@Qualifier("vcHtmlLoader")
	private ILoader vcHtmlLoader;
	
	public boolean updateNews() 
	{
		MainLogger.logger.fine("NewsService updateNews() start");
		
		try
		{	
			List<Callable<Void>> tasks = new ArrayList<>();
			tasks.add(new NewsService.NewsStarter(riaNewsLoader, RiaNewsHtmlParser.class));
			tasks.add(new NewsService.NewsStarter(starhitHtmlLoader, StarhitHtmlParser.class)); 
			tasks.add(new NewsService.VcStarter(vcHtmlLoader, VcHtmlParser.class)); 	
			tasks.add(new NewsService.HoroscopeStarter(HoroscopeHtmlLoaderParser.class));
			tasks.add(new NewsService.RutubeStarter(RutubeHtmlLoaderParser.class));
			tasks.add(new NewsService.WikipediaStarter(WikipediaHtmlLoaderParser.class));
			
			ExecutorService executor = Executors.newCachedThreadPool();
			executor.invokeAll(tasks);
			executor.shutdown();
			
		}
		catch(Exception ex)
		{
			MainLogger.logger.warning("NewsService updateNews() exception: " + ex);			
		}		
		
		MainLogger.logger.fine("NewsService updateNews() finish");
		
		return true;
		
	}
	
	private class NewsStarter implements IStarter{
		
		private ILoader loader;
		private Class<? extends IParser> parserClass;
		
		NewsStarter(ILoader loader, Class<? extends IParser> parserClass)
		{
			this.loader = loader;
			this.parserClass = parserClass;
		}
		
		@Override
		public Void call() 
		{
			try
			{
				ConcurrentLinkedQueue<String> links = (ConcurrentLinkedQueue<String>)loader.load();		
				if(links==null) return null;
				
				ExecutorService executor = Executors.newCachedThreadPool();
				ConcurrentLinkedQueue<News> items = new ConcurrentLinkedQueue<>();
				List<Callable<Void>> tasks = new LinkedList<>();
				
				for(int i=0; i<links.size(); i++)
				{
					var parser = parserClass.getDeclaredConstructor(Queue.class, Collection.class).newInstance(links, items);
					
					InvocationHandler handler = new ParserInvocationHandler(parser);
					IParser proxy = (IParser) Proxy.newProxyInstance(IParser.class.getClassLoader(), 
							new Class<?>[] {IParser.class}, handler);
							
					tasks.add(proxy);
				}		
				
				executor.invokeAll(tasks);
				executor.shutdown();
				
				MainLogger.logger.fine("NewsStarter total news:" + items.size());			
				
				ExecutorService executor2 = Executors.newCachedThreadPool();			
				List<Callable<Void>> tasks2 = new LinkedList<>();
				
				for(var item : items)
					tasks2.add(()->{		
						
						if(parserClass==RiaNewsHtmlParser.class)	
							repo.insertRiaNews(item);
						else
							repo.insertStarhitNews(item);
						
						return null;					
					});
				
				executor2.invokeAll(tasks2);
				executor2.shutdown();
			}
			catch(Exception ex)
			{
				MainLogger.logger.warning("******** NewsService.NewsStarter exception:" + ex.getMessage());
			}
			
			return null;
		}
		
	}
	
	private class VcStarter implements IStarter{
		
		private ILoader loader;
		private Class<? extends IParser> parserClass;
		
		VcStarter(ILoader loader, Class<? extends IParser> parserClass)
		{
			this.loader = loader;
			this.parserClass = parserClass;
		}
		
		@Override
		public Void call() 
		{
			try
			{
				ConcurrentLinkedQueue<String> links = (ConcurrentLinkedQueue<String>)loader.load();		
				if(links==null) return null;
				
				ExecutorService executor = Executors.newCachedThreadPool();
				ConcurrentLinkedQueue<VcArticle> items = new ConcurrentLinkedQueue<>();
				List<Callable<Void>> tasks = new LinkedList<>();
				
				for(int i=0; i<links.size(); i++)
				{
					var parser = parserClass.getDeclaredConstructor(Queue.class, Collection.class).newInstance(links, items);
					
					InvocationHandler handler = new ParserInvocationHandler(parser);
					IParser proxy = (IParser) Proxy.newProxyInstance(IParser.class.getClassLoader(), 
							new Class<?>[] {IParser.class}, handler);
							
					tasks.add(proxy);
				}		
				
				executor.invokeAll(tasks);
				executor.shutdown();
				
				MainLogger.logger.fine("VcStarter total articles:" + items.size());			
				
				ExecutorService executor2 = Executors.newCachedThreadPool();			
				List<Callable<Void>> tasks2 = new LinkedList<>();
				
				for(var item : items)
					tasks2.add(()->{					
						repo.insertVcArticle(item);
						return null;					
					});
				
				executor2.invokeAll(tasks2);
				executor2.shutdown();
			}
			catch(Exception ex)
			{
				MainLogger.logger.warning("******** NewsService.VcStarter exception:" + ex.getMessage());
			}
			
			return null;
		}
		
	}
	
	private class HoroscopeStarter implements IStarter{		
		
		private Class<? extends IParser> parserClass;
		
		HoroscopeStarter(Class<? extends IParser> parserClass)
		{			
			this.parserClass = parserClass;
		}
		
		@Override
		public Void call() //throws Exception
		{	
			try
			{
			
				MainLogger.logger.fine("NewsService.HoroscopeStarter start");
				
				ExecutorService executor = Executors.newCachedThreadPool();
				ConcurrentLinkedQueue<Horoscope> items = new ConcurrentLinkedQueue<>();
				List<Callable<Void>> tasks = new LinkedList<>();
				
				for(int i=0; i<12; i++)
				{					
					var parser = parserClass.getDeclaredConstructor(Collection.class).newInstance(items);
					
					InvocationHandler handler = new ParserInvocationHandler(parser);
					IParser proxy = (IParser) Proxy.newProxyInstance(IParser.class.getClassLoader(), 
							new Class<?>[] {IParser.class}, handler);	
							
					tasks.add(proxy);
				}		
				
				MainLogger.logger.fine("NewsService.HoroscopeStarter total tasks: " + tasks.size());
				
				executor.invokeAll(tasks);
				executor.shutdown();
				
				MainLogger.logger.fine("HoroscopeStarter total articles:" + items.size());			
				
				ExecutorService executor2 = Executors.newCachedThreadPool();			
				List<Callable<Void>> tasks2 = new LinkedList<>();
				
				for(var item : items)
					tasks2.add(()->{					
						repo.insertHoroscope(item);
						return null;					
					});
				
				executor2.invokeAll(tasks2);
				executor2.shutdown();
			
			}
			catch(Exception ex)
			{
				MainLogger.logger.warning("******** NewsService.HoroscopeStarter exception:" + ex.getMessage());
			}
			
			MainLogger.logger.fine("NewsService.HoroscopeStarter success");
			
			return null;
		}
		
	}
	
	private class RutubeStarter implements IStarter
	{
		private Class<? extends IParser> parserClass;
		
		RutubeStarter(Class<? extends IParser> parserClass)
		{			
			this.parserClass = parserClass;
		}
		
		@Override
		public Void call() 		
		{
			try
			{
				List<Video> videos = new LinkedList<>();
				
				var parser = parserClass.getDeclaredConstructor(Collection.class).newInstance(videos);
				
				InvocationHandler handler = new ParserInvocationHandler(parser);
				IParser proxy = (IParser) Proxy.newProxyInstance(IParser.class.getClassLoader(), 
						new Class<?>[] {IParser.class}, handler);
				
				ExecutorService executor = Executors.newSingleThreadExecutor();
				var f = executor.submit(proxy);
				f.get();
				executor.shutdown();
				
				MainLogger.logger.fine("RutubeStarter total videos:" + videos.size());			
				
				ExecutorService executor2 = Executors.newCachedThreadPool();			
				List<Callable<Void>> tasks2 = new LinkedList<>();
				
				for(var item : videos)
					tasks2.add(()->{					
						repo.insertVideo(item);
						return null;					
					});
				
				executor2.invokeAll(tasks2);
				executor2.shutdown();
			}
			catch(Exception ex)
			{
				MainLogger.logger.warning("******** NewsService.RutubeStarter exception:" + ex.getMessage());
			}
			
			return null;			
		}
	}
	
	
	private class WikipediaStarter implements IStarter
	{
		private Class<? extends IParser> parserClass;
		
		WikipediaStarter(Class<? extends IParser> parserClass)
		{			
			this.parserClass = parserClass;
		}
		
		@Override
		public Void call() 		
		{
			try
			{					
				StringBuilder builder = new StringBuilder();
				var parser = parserClass.getDeclaredConstructor(StringBuilder.class).newInstance(builder);
				
				InvocationHandler handler = new ParserInvocationHandler(parser);
				IParser proxy = (IParser) Proxy.newProxyInstance(IParser.class.getClassLoader(), 
						new Class<?>[] {IParser.class}, handler);
				
				ExecutorService executor = Executors.newSingleThreadExecutor();
				var f = executor.submit(proxy);
				f.get();
				executor.shutdown();		
				
				MainLogger.logger.fine("******** WikipediaStarter day event:"+builder.toString());
				
				ExecutorService executor2 = Executors.newCachedThreadPool();			
				List<Callable<Void>> tasks2 = new LinkedList<>();
				
				tasks2.add(()->{					
						repo.insertWiki(builder.toString());
						return null;					
					});
				
				executor2.invokeAll(tasks2);
				executor2.shutdown();
				
			}
			catch(Exception ex)
			{
				MainLogger.logger.warning("******** NewsService.WikipediaStarter exception:" + ex.getMessage());
			}
			
			return null;			
		}
	}
	

}
