package com.socialapp.main.Converters;

import org.springframework.core.convert.converter.Converter;

public class String2LongConverter implements Converter<String, Long>{
	
	public Long convert(String s)
	{
		return Long.valueOf(s);
	}

}
