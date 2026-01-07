package com.socialapp.main;

import java.util.Locale;

import org.springframework.expression.ParseException;
import org.springframework.format.Formatter;

import com.socialapp.main.Enums.MaritalStatus;

public class MaritalStatusFormatter implements Formatter<MaritalStatus>{
	
	@Override
	public MaritalStatus parse(String text, Locale locale) throws ParseException
	{
		switch(text)
		{
			case "married": return MaritalStatus.Married;
			case "notmarried": return MaritalStatus.Not_Married;
			default: throw new ParseException(0, "MaritalStatus is wrong");
		}
	}
	
	@Override
	public String print(MaritalStatus status, Locale locale)
	{
		switch(status)
		{
			case Married: return "married";
			default: return "notmarried";			
		}
	}

}
