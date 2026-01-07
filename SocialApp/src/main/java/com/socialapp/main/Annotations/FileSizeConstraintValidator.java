package com.socialapp.main.Annotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.multipart.MultipartFile;

import com.socialapp.main.ConfigurationProperties.FileSizeProperties;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class FileSizeConstraintValidator implements ConstraintValidator<FileSize, MultipartFile> {
	
	@Autowired
	private FileSizeProperties props;
	
	private Logger logger = LoggerFactory.getLogger(FileSizeConstraintValidator.class);
			
	//@Override
	public boolean isValid(MultipartFile file, ConstraintValidatorContext context)
	{
		logger.debug("started");
		
		int min = props.getMinsize();
		int max = props.getMaxsize();
		
		logger.debug("min filesize:" + min);
		logger.debug("max filesize:" + max);	
				
		long fileSize = file.getSize(); // bytes
		
		if(fileSize<min) return false;
		if(fileSize>max) return false;		
		
		return true;				
				
	}

}
