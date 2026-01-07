package com.socialapp.main.Annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/*
 * Используется для ограничения входящей строки символами [a-zA-Z0-9]
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy=LatinNumCharsConstraintValidator.class)
public @interface LatinNumChars {
	
	String message() default "latinnumchars";  //!!! MessageSource не распознает
	Class<?>[] groups() default {}; 
	Class<? extends Payload>[] payload() default {};
}
