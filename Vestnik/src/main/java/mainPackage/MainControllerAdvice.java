package mainPackage;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/*
 * Обрабатывает исключения из класса MainController
 */
@ControllerAdvice(assignableTypes= {MainController.class})
public class MainControllerAdvice {
	
	@ExceptionHandler
	public String handler(Exception ex)
	{
		MainLogger.logger.warning("MainControllerAdvice:" + ex.getMessage());
		if(ex.getCause()!=null) 
			MainLogger.logger.warning("MainControllerAdvice cause:" + ex.getCause().getMessage());
		
		return "redirect:/";
	}

}
