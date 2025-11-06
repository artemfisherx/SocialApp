package mainPackage.Services;

import java.io.File;
import java.net.InetAddress;

import org.springframework.core.io.ClassPathResource;

import com.maxmind.geoip2.DatabaseReader;

import mainPackage.MainLogger;

public class GeoLocationService {
	
	public static String getCity(InetAddress ip)
	{
		try
		{
			ClassPathResource path = new ClassPathResource("GeoLite2-City.mmdb");			
			File file = path.getFile();
			DatabaseReader reader =  new DatabaseReader.Builder(file).build();
			var resp = reader.city(ip);
			
			return resp.getCity().getName();
		}
		catch(Exception ex)
		{
			MainLogger.logger.warning(ex.getMessage());
		}
		
		return "";
		
	}

}
