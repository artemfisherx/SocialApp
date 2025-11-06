package mainPackage;

import org.springframework.core.metrics.jfr.FlightRecorderApplicationStartup;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;

public class Vestnik extends AbstractAnnotationConfigDispatcherServletInitializer{
		
	private final String file_dir = "C:/Program Files/Apache Software Foundation/Tomcat 10.1/webapps/VestnikFileDir";
	private int max_file_size = 1_000_000;
	private int max_request_size = 1_500_000;
	private int file_size_threshold = 500_000;
	
	
	@Override
	protected Class<?>[] getRootConfigClasses(){
		return new Class<?>[]{MainConfig.class};
	} 
	
	@Override
	protected Class<?>[] getServletConfigClasses(){
		return new Class<?>[] {MainWebConfig.class};
	}
	
	@Override
	protected String[] getServletMappings()
	{
		return new String[] {"/"};
	}
	
	@Override
	protected String getServletName() {
		return "main";
	}
	
	@Override
	protected WebApplicationContext createRootApplicationContext()
	{
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(getRootConfigClasses());
		context.setApplicationStartup(new FlightRecorderApplicationStartup());
		return context;
	}
	
	@Override
	protected void customizeRegistration(ServletRegistration.Dynamic registration)
	{
		registration.setMultipartConfig(new 
				MultipartConfigElement(file_dir, max_file_size, max_request_size, file_size_threshold));
		registration.setInitParameter("enableLoggingRequestDetails", "true");
	}
	
	@Override
	protected Filter[] getServletFilters()
	{
		return new Filter[] {
				new FormContentFilter(),
				new ForwardedHeaderFilter(),
				new ShallowEtagHeaderFilter(),
				new ResourceUrlEncodingFilter()
				};
	}
}
