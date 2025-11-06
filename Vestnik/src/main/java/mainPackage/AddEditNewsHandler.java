package mainPackage;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import jakarta.servlet.http.Part;
import mainPackage.Entities.News;
import mainPackage.Formatters.OffsetDateTimeFormatter;
import mainPackage.Interfaces.IRepository;

/*
 * Обработчик добавления и редактирования новостей. Используется в бине типа RouterFunction.
 */
public class AddEditNewsHandler {
	
	private final String file_dir = "files";
		
	private IRepository repo;	
		
	private SimpMessagingTemplate sender;
	
	public AddEditNewsHandler(IRepository repo, SimpMessagingTemplate sender)
	{
		this.repo = repo;	
		this.sender = sender;
	}
	
	public ServerResponse addNews(ServerRequest request) throws Exception
	{
		News news = new News();
		news.setId(0);
		return ServerResponse.ok().render("addEditNews", news);		
	}
	
	public ServerResponse editNews(ServerRequest request) throws Exception
	{
		int id = Integer.parseInt(request.pathVariable("id"));
		News news = repo.getRiaNews(id);
		return ServerResponse.ok().render("addEditNews", news);
	}
	
	public ServerResponse saveNews(ServerRequest request)
	{
		MainLogger.logger.fine("AddEditNewsHandler saveNews start");
		String contextPath = request.servletRequest().getServletContext().getContextPath();	 
		
		try
		{								
			News news = request.bind(News.class, b ->
				{
					b.addCustomFormatter(new OffsetDateTimeFormatter());	
				});											
			
			Part img = request.multipartData().getFirst("file");						
						
			if(img!=null)
			{
				Instant now = Instant.now();
				
				int startIndex = img.getSubmittedFileName().indexOf(".");
				int finishIndex = img.getSubmittedFileName().length();
				String ext = img.getSubmittedFileName().substring(startIndex, finishIndex);
				
				String newFileName = now.getEpochSecond() + now.getNano() + ext;
				
				String realPath = request.servletRequest().getServletContext().getRealPath(file_dir);
				
				if(!Files.exists(Path.of(realPath)))
					Files.createDirectory(Path.of(realPath));
				
				Files.copy(img.getInputStream(), Path.of(realPath, newFileName));
				
				news.setImg("/" + file_dir + "/" + newFileName);
			}
								
			
			if(news.getId()==0)
			{
				repo.insertRiaNews(news);				
				sender.convertAndSend("/topic/news", news); //отправляем в брокер
			}				
			else
				repo.updateRiaNews(news);		
			
			MainLogger.logger.fine("AddEditNewsHandler saveNews success");
		}
		catch(Exception ex)
		{
			MainLogger.logger.warning(ex.getMessage());
		}
		
		MainLogger.logger.fine("AddEditNewsHandler saveNews finish");
		
		
		return ServerResponse.permanentRedirect(URI.create(contextPath)).build();
	}
	

}
