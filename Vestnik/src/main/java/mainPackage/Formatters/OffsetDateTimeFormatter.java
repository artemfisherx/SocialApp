package mainPackage.Formatters;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.format.Formatter;

import mainPackage.MainLogger;

public class OffsetDateTimeFormatter implements Formatter<OffsetDateTime>{
	
	@Override
	public String print(OffsetDateTime dt, Locale locale)
	{
		return dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
	}
	
	/*
	 * Используется при парсинге данных с формы страницы addEditNews
	 */
	@Override
	public OffsetDateTime parse(String text, Locale locale)
	{	
		LocalDateTime ld = LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
		
		return OffsetDateTime.of(ld, ZoneOffset.ofHours(3));
	}

}
