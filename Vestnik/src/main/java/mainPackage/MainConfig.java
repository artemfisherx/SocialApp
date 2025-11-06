package mainPackage;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ComponentScan
@EnableCaching
public class MainConfig {
	
	@Value("${url}") String url;
	@Value("${user}") String user;
	@Value("${password}") String password;
	
	
	@Bean
	public DataSource dataSource() 
	{
		BasicDataSource source = new BasicDataSource();
		source.setDriver(new Driver());
		source.setUrl(url);
		source.setUsername(user);
		source.setPassword(password);		
		return source;
	}
	
	@Bean
	public PlatformTransactionManager transactionManager()
	{
		DataSourceTransactionManager manager = new DataSourceTransactionManager();
		manager.setDataSource(dataSource());
		return manager;
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
	{
		var ps = new PropertySourcesPlaceholderConfigurer();
		ps.setLocation(new ClassPathResource("dbconfig.properties"));
		return ps;
	}
	
	@Bean
	public MessageSource messageSource()
	{
		ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
		ms.setBasename("messages");		
		return ms;
	}
	
	@Bean 
	public CacheManager cacheManager()
	{
		return new ConcurrentMapCacheManager();
	}

}
