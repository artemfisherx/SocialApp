package mainPackage.Rest;

import java.net.InetAddress;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.inject.Inject;
import mainPackage.MainLogger;
import mainPackage.Entities.News;
import mainPackage.Interfaces.IRepository;
import mainPackage.Services.GeoLocationService;

@Controller
@RequestMapping("/rest/ria")
public class RiaNewsRestController {
	
	@Inject
	private IRepository repo;
		
		
	@GetMapping("/{id}")	
	ResponseEntity<News> getRiaNews(@PathVariable("id") int id) throws RiaNewsRestException 
	{
		MainLogger.logger.fine("RiaNewsRestController getRiaNews() id: " + id);
		
		try
		{
			var news = repo.getRiaNews(id);
			
			MainLogger.logger.fine("RiaNewsRestController getRiaNews() news: " + news.getTitle());
			
			return new ResponseEntity<>(news, HttpStatusCode.valueOf(200));
		}
		catch(Exception ex)
		{
			MainLogger.logger.warning("RiaNewsRestController getRiaNews() exception: " + ex.getMessage());
			throw new RiaNewsRestException(ex.getMessage());
		}	
		
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.OK)
	HttpEntity<News> postRiaNews(@RequestBody News news, BindingResult result) throws RiaNewsRestException
	{
		if(result.hasErrors())
			result.getAllErrors().stream().forEach(o->MainLogger.logger.warning(o.toString()));
		
		try
		{
			int id = repo.insertRiaNews(news);
			var newNews = repo.getRiaNews(id);
			return new HttpEntity<>(newNews);
		}
		catch(Exception ex)
		{
			MainLogger.logger.warning("RiaNewsRestController postRiaNews() exception: " + ex.getMessage());
			throw new RiaNewsRestException(ex.getMessage());
		}
		
	}
	
	@PutMapping
	@ResponseBody
	News putRiaNews(@RequestBody News news, BindingResult result) throws RiaNewsRestException
	{
		if(result.hasErrors())
			result.getAllErrors().stream().forEach(o->MainLogger.logger.warning(o.toString()));
		try
		{
			repo.updateRiaNews(news);		
			return repo.getRiaNews(news.getId());
		}
		catch(Exception ex)
		{
			MainLogger.logger.warning("RiaNewsRestController putRiaNews() exception: " + ex.getMessage());
			throw new RiaNewsRestException(ex.getMessage());
		}
		
	}
	
	@DeleteMapping("/{id}")
	@ResponseBody
	void deleteRiaNews(@PathVariable("id") int id) throws RiaNewsRestException
	{
		try
		{
			repo.deleteRiaNews(id);
		}
		catch(Exception ex)
		{
			MainLogger.logger.warning("RiaNewsRestController deleteRiaNews() exception: " + ex.getMessage());
			throw new RiaNewsRestException(ex.getMessage());
		}
		
	}	
}
