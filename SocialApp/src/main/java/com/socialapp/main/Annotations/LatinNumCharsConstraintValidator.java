package com.socialapp.main.Annotations;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LatinNumCharsConstraintValidator implements ConstraintValidator<LatinNumChars, String>{
	
	@Override
	public boolean isValid(String in, ConstraintValidatorContext context)
	{	
		return Pattern.matches("^[a-zA-Z0-9]+$", in);		
			
	}

}
