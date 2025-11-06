package mainPackage.Rest;

import java.util.Locale;

import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.inject.Inject;

@RestControllerAdvice(assignableTypes= {RiaNewsRestController.class})
public class MainRestControllerAdvice //extends ResponseEntityExceptionHandler
{

	@Inject
	MessageSource msg;	
	
	@ExceptionHandler({RiaNewsRestException.class})
	ErrorResponse handler(RiaNewsRestException ex)
	{
		return ErrorResponse.builder(ex, HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error")				
				.build(msg, Locale.getDefault());
	}	

}
