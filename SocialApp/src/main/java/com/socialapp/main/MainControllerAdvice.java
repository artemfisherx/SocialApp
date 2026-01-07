package com.socialapp.main;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@ControllerAdvice
public class MainControllerAdvice {

	@ExceptionHandler
	public String handleValidationException(HandlerMethodValidationException ex)
	{
		System.out.println("====MainControllerAdvice handleValidationException");
		return "redirect:/photos?sizeover";
	}
}
