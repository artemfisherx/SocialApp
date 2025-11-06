package mainPackage;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.WebContentInterceptor;
import org.springframework.web.servlet.resource.CachingResourceResolver;
import org.springframework.web.servlet.resource.ContentVersionStrategy;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

import mainPackage.Formatters.OffsetDateTimeFormatter;
import mainPackage.Interfaces.IRepository;

@Configuration
@EnableWebMvc
@ComponentScan
@EnableAspectJAutoProxy
public class MainWebConfig implements WebMvcConfigurer{
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	private SimpMessagingTemplate sender;
	
	@Autowired
	@Qualifier("mainRepository")
	IRepository repo;
	
	@Bean
	public SpringResourceTemplateResolver templateResolver(){
	    // SpringResourceTemplateResolver automatically integrates with Spring's own
	    // resource resolution infrastructure, which is highly recommended.
	    SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
	    templateResolver.setApplicationContext(this.applicationContext);
	    templateResolver.setPrefix("/WEB-INF/views/");
	    templateResolver.setSuffix(".html");
	    templateResolver.setCharacterEncoding("UTF-8");
	    // HTML is the default value, added here for the sake of clarity.
	    templateResolver.setTemplateMode(TemplateMode.HTML);
	    // Template cache is true by default. Set to false if you want
	    // templates to be automatically updated when modified.
	    templateResolver.setCacheable(false);
	    return templateResolver;
	}

	@Bean
	public SpringTemplateEngine templateEngine(){
	    // SpringTemplateEngine automatically applies SpringStandardDialect and
	    // enables Spring's own MessageSource message resolution mechanisms.
	    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
	    templateEngine.setTemplateResolver(templateResolver());
	    // Enabling the SpringEL compiler with Spring 4.2.4 or newer can
	    // speed up execution in most scenarios, but might be incompatible
	    // with specific cases when expressions in one template are reused
	    // across different data types, so this flag is "false" by default
	    // for safer backwards compatibility.
	    templateEngine.setEnableSpringELCompiler(true);
	    return templateEngine;
	}
	
	@Bean
	public ThymeleafViewResolver viewResolver(){
	    ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
	    viewResolver.setTemplateEngine(templateEngine());
	    viewResolver.setCharacterEncoding("UTF-8");
	    viewResolver.setContentType("text/html; charset=UTF-8");	    
	    // NOTE 'order' and 'viewNames' are optional
	    viewResolver.setOrder(1);	    
	    return viewResolver;
	}	
		
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		 VersionResourceResolver versionResourceResolver = new VersionResourceResolver()
			        .addVersionStrategy(new ContentVersionStrategy(), "/**");
		
		registry.addResourceHandler("/images/**")
			.addResourceLocations("/resources/images/")
			.setCachePeriod(0)
			.resourceChain(true)			
			.addResolver(versionResourceResolver);
			
		registry.addResourceHandler("/cleditor/**")
			.addResourceLocations("/resources/cleditor/")
			.setCachePeriod(0);
		
		registry.addResourceHandler("/files/**")
			.addResourceLocations("/files/")
			.setCachePeriod(0);		
		
		registry.addResourceHandler("/webjars/**")
				.addResourceLocations("/webjars/");
	}
	
		
	@Override
	public void addFormatters(FormatterRegistry registry)
	{
		registry.addFormatter(offsetDateTimeFormatter());
	}
	
	@Bean
	OffsetDateTimeFormatter offsetDateTimeFormatter()
	{
		return new OffsetDateTimeFormatter();
	}
	
	@Bean
	RouterFunction<ServerResponse> newsRouter()
	{
		AddEditNewsHandler handler = new AddEditNewsHandler(repo, sender);
		return RouterFunctions.route().path("/news", builder->builder
				.GET("/add", handler::addNews)
				.GET("/edit/{id}", handler::editNews)
				.POST("/save", handler::saveNews)				
				)
				.after((req, resp)->
				{
					MainLogger.logger.fine("RouterFunction finish");
					return resp;
				})
				.build();
	}
	
	@Bean
	LocaleResolver localeResolver()
	{
		return new SessionLocaleResolver();		
	}
	
	@Bean
	LocaleChangeInterceptor localeChangeInterceptor()
	{
		LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		lci.setParamName("lang");
		return lci;
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry)
	{
		WebContentInterceptor wci = new WebContentInterceptor();
		wci.addCacheMapping(CacheControl.maxAge(1, TimeUnit.HOURS).mustRevalidate(), "/*");
		
		registry.addInterceptor(localeChangeInterceptor());
		//  registry.addInterceptor(wci);
	}
	
	@Bean
	MultipartResolver multipartResolver()
	{
		return new StandardServletMultipartResolver();
		
	}
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry)
	{
		registry.addViewController("/contact").setViewName("contact");
	}
	
	
}
