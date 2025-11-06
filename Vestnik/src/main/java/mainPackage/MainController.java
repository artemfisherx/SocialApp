package mainPackage;

import java.lang.ref.SoftReference;
import java.net.InetAddress;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.inject.Inject;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mainPackage.Entities.Horoscope;
import mainPackage.Entities.News;
import mainPackage.Entities.SearchResult;
import mainPackage.Entities.VcArticle;
import mainPackage.Entities.Video;
import mainPackage.Enums.HoroscopeSign;
import mainPackage.Interfaces.IRepository;
import mainPackage.Services.GeoLocationService;
import mainPackage.Services.NewsService;
import mainPackage.UpdateCheckers.RiaNewsUpdateChecker;

@Controller
@RequestMapping("/")
public class MainController {
	
	@Autowired
	private NewsService newsService;
	
	@Autowired
	private ApplicationStartup startup;
	
	@Autowired
	@Qualifier("mainRepository")
	private IRepository repo;
	
	@Inject
	private RiaNewsUpdateChecker riaNewsChecker;
	
	@Inject
	private MessageSource msgSource;
	
	//Используется в качестве кэша для новостей из РИА Новости
	private SoftReference<HashMap<Integer, News>> riaNewsCache = new SoftReference<>(null);
	
	@GetMapping("/")
	String showIndexPage(HttpServletRequest request, HttpServletResponse response) {
		StartupStep step = startup.start("indexStep");
		step.tag("result", "success");
		step.end();
		
		return "index";			
	}
	
	@GetMapping("/update")
	@ResponseBody
	void update() throws Exception
	{	
		
		MainLogger.logger.fine("MainController Update() start");
		
		newsService.updateNews();	
		repo.insertLastUpdate();
		
		MainLogger.logger.fine("MainController Update() finish");
	}
		
	
	@GetMapping("/ria/{id}")
	String getRiaNews(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") int id, Model model)  throws Exception
	{	
		News news;
		var refMap = riaNewsCache.get();		
		if(refMap!=null)
		{			
			MainLogger.logger.info("------------getRiaNews cache1");
			
			news = refMap.get(id);
			
			if(news==null)
			{
				MainLogger.logger.info("------------getRiaNews cache2");
				news = repo.getRiaNews(id);
				refMap.put(id, news);
			}	
			
			riaNewsCache = new SoftReference<HashMap<Integer, News>>(refMap);	
			refMap=null;
		}
		else
		{
			MainLogger.logger.info("------------getRiaNews cache3");
			
			news = repo.getRiaNews(id);
			
			var map = new HashMap<Integer, News>();
			map.put(news.getId(), news);
			riaNewsCache = new SoftReference<HashMap<Integer, News>>(map);
			map=null;
		}
		
		model.addAttribute("news", news);
		return "riaNews";		
		
		/* старая реализация
		var news = repo.getRiaNews(id);
		model.addAttribute("news", news);
		return "riaNews";	
		*/
	}
	
	@GetMapping("/starhit/{id}")	
	String getStarhitNews(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") int id, Model model)  throws Exception
	{
		var news = repo.getStarhitNews(id);
		model.addAttribute("news", news);
		return "starhitNews";				
	}
	
	@GetMapping("/vc/{id}")
	String getVcArticle(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") int id, Model model)  throws Exception
	{
		var news = repo.getVcArticle(id);
		model.addAttribute("article", news);
		return "vc";				
	}
	
	@GetMapping("/videos/{id}")
	String getVideo(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") int id, Model model)  throws Exception
	{
		String link = repo.getVideo(id).getLink();
		
		Pattern pattern = Pattern.compile("/(\\w*?)/$");
		Matcher matcher = pattern.matcher(link);
		
		String video="";
		
		if(matcher.find())
		{
			video = matcher.group(1);
			MainLogger.logger.fine("Video link id: " + video);
		}			
		else
			MainLogger.logger.fine("Video link id is empty");
		
		model.addAttribute("video", video);
		return "videos";				
	}
	
	@GetMapping("/search")
	String getSearch(HttpServletRequest request, HttpServletResponse response, @RequestParam("search_text") String text, @RequestParam(value="pagenum", required=false) Integer pageNum, Model model) throws Exception
	{	
		if(pageNum==null)
		{
			pageNum=Integer.valueOf(1);
			
		}
			
		
		MainLogger.logger.fine("MainController getSearch start");
		
		//int pageNum=1;
		
		int pageSize=10;
		
		
		SearchResult result = repo.search(text, pageNum, pageSize);
		model.addAttribute("result", result);
						
		MainLogger.logger.fine("MainController getSearch result:"+result.getItems().size());
		MainLogger.logger.fine("MainController getSearch finish");
		
		return "search";
	}
	
	
	//@RequestMapping("/error") временно отключил error-page
	String getErrorPage(HttpServletRequest request)
	{
		MainLogger.logger.warning("MainController getErrorPage(): " 
							+ request.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
		return "error";
	}
	
	@GetMapping("/horoscope")
	ResponseEntity<String> getHoroscope(@RequestParam("sign")String sign) throws Exception
	{
		MainLogger.logger.fine("MainController getHoroscope start");
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		
		String text = repo.getHoroscope(HoroscopeSign.valueOf(sign)).getText();
		return new ResponseEntity<String>(text, headers, HttpStatusCode.valueOf(200));
	}
	
	/*
	@GetMapping("/checkRiaNews")
	SseEmitter checkRiaNews()
	{
		MainLogger.logger.info("MainController checkRiaNews() start");
		
		long hours = 1;
		long timeout = hours*60*1000;
		
		SseEmitter emitter = new SseEmitter(timeout);		
		
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(()->
		{
			try
			{
				OffsetDateTime lastUpdateDt = repo.getLastUpdate().toOffsetDateTime();
				boolean has = riaNewsChecker.hasUpdate(lastUpdateDt);
				
				if(has)
					emitter.send("1");
				else
				{
					emitter.send("");
					emitter.complete(); // отключает эмиттер. Убрать для включения бесконечного SSE
				}
						
				
			}
			catch(Exception ex)
			{
				MainLogger.logger.warning("MainController checkRiaNews() exception: " + ex.getMessage());
			}						
			
		});					
		
		return emitter;
		
	}
	*/
	
	
	@GetMapping("/hometown")
	@ResponseBody
	String getHometown(HttpServletRequest request)
	{
		try
		{
			//String addr = request.getRemoteAddr(); на время разработки отключил;
			
			String addr = "47.21.241.1";
			InetAddress ip = InetAddress.getByName(addr);			
			return GeoLocationService.getCity(ip);
		}
		catch(Exception ex)
		{
			MainLogger.logger.warning(ex.getMessage());
		}
		
		return "";
	}
		
	
	/***************** Model attributes ************************/
	
	@ModelAttribute("riaNews")
	List<News> getLastRiaNews() throws Exception
	{		
		var list = repo.getLastRiaNews();
		MainLogger.logger.fine("MainController riaNews count:"+list.size());
		
		return list;
	}
	
	@ModelAttribute("starhitNews")
	List<News> getLastStarhitNews() throws Exception
	{		
		var list = repo.getLastStarhitNews();
		MainLogger.logger.fine("MainController starhitNews count:"+list.size());
		
		return list;
	}
	
	@ModelAttribute("vcArticles")
	List<VcArticle> getLastVcArticles() throws Exception
	{		
		var list = repo.getLastVcArticles();
		MainLogger.logger.fine("MainController vcArticles count:"+list.size());
		
		return list;
	}
	
	@ModelAttribute("dayEvents")
	String getLastDayEvents() throws Exception
	{
		String events = repo.getLastDayEvents();
		MainLogger.logger.fine("MainController dayEvents length:" + events.length());
		
		return events;
	}
	
	@ModelAttribute("horoscope")
	Horoscope getLastHoroscope() throws Exception
	{
		Horoscope horo = repo.getHoroscope(HoroscopeSign.aries);
		MainLogger.logger.fine("MainController horoscope:" + horo);
		return horo;
	}
	
	@ModelAttribute("videos")
	List<Video> getLastVideos() throws Exception
	{
		List<Video> videos = repo.getLastVideos();
		MainLogger.logger.fine("MainController rutube count:" + videos.size());
		return videos;
	}
	
	@ModelAttribute("lastUpdate")
	String getLastUpdate() throws Exception
	{
		ZonedDateTime dt = repo.getLastUpdate();
		
		if(dt==null) return null;
		
		String str = dt.format(DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy"));
		MainLogger.logger.fine("MainController last update:" + dt);
		return str;
	}
	
}
