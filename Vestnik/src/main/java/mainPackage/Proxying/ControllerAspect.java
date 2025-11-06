package mainPackage.Proxying;

import java.util.Arrays;
import java.util.Optional;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mainPackage.MainLogger;

/*
 * Перехватывает указанные методы контролеров
 */

@Aspect
@Component
public class ControllerAspect {
	
	@Before(value="execution(* mainPackage.MainController.showIndexPage(..))&&args(request, response)",
			argNames = "request, response")
	void showIndexPageAspect(HttpServletRequest request, HttpServletResponse response)
	{		
		saveVisit(request, response);
	}
	
	@Before(value="execution(* mainPackage.MainController.getRiaNews(..))"
			+ "&&args(request, response, id, model)",
			argNames="request, response, id, model")
	void getRiaNewsAspect(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") int id, Model model)
	{		
		saveVisit(request, response);
	}
	
	@Before(value="execution(* mainPackage.MainController.getStarhitNews(..))"
			+ "&&args(request, response, id, model)",
			argNames="request, response, id, model")
	void getStarhitNewsAspect(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") int id, Model model)
	{		
		saveVisit(request, response);
	}
	
	@Before(value="execution(* mainPackage.MainController.getVcArticle(..))"
			+ "&&args(request, response, id, model)",
			argNames="request, response, id, model")
	void getVcArticleAspect(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") int id, Model model)
	{		
		saveVisit(request, response);
	}
	
	@Before(value="execution(* mainPackage.MainController.getVideo(..))"
			+ "&&args(request, response, id, model)",
			argNames="request, response, id, model")
	void getVideoAspect(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") int id, Model model)
	{
		saveVisit(request, response);
	}
	
	//СДЕЛАТЬ ПОСЛЕ ТОГО КАК СДЕЛАЮ ПОИСК
	@Before(value="execution(* mainPackage.MainController.getVideo(..))&&args(request, response)",
			argNames = "request, response")
	void getSearchAspect(HttpServletRequest request, HttpServletResponse response)
	{
		saveVisit(request, response);
	}
	
	/*
	 * Сохраняет историю посещения
	 */
	private void saveVisit(HttpServletRequest request, HttpServletResponse response)
	{		
		Cookie [] cookies = request.getCookies();
		String userIp="";
		if(cookies!=null)
		{
			Optional<Cookie> userCookieIp = Arrays.asList(cookies).stream().filter(o->o.getName().equalsIgnoreCase("user_ip")).findFirst();
						
			if(userCookieIp.isPresent()) 
				userIp = userCookieIp.get().getValue();			
			else
				userIp = request.getRemoteAddr();
		}
		else
		{
			userIp = request.getRemoteAddr();
			
			Cookie cookie = new Cookie("user_ip", userIp);
			response.addCookie(cookie);
		}
						
		String uri = request.getRequestURI();		
		
		MainLogger.logger.fine("---------------ControllerAspect:" + userIp + " " + uri);
			
	}
	
	

}
