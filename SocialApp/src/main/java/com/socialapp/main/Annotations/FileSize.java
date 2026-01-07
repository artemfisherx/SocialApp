package com.socialapp.main.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AliasFor;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/*
 * Используется для ограничения размера загружаемого файла. Единица измерения - байт.
 */

@Target({ElementType.FIELD, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=FileSizeConstraintValidator.class)
public @interface FileSize {	
	
	String message() default "file.size";  //!!! MessageSource не распознает
	Class<?>[] groups() default {}; 
	Class<? extends Payload>[] payload() default {};	
}
