package com.socialapp.main;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.SpringCacheBasedAclCache;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ott.JdbcOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationProvider;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.web.servlet.DispatcherServlet;

import com.socialapp.main.Security.CheckAuthenticationFilter;
import com.socialapp.main.Security.DatabaseRequestCache;
import com.socialapp.main.Security.MyJdbcSessionRegistry;
import com.socialapp.main.Security.MyJdbcSessionRegistryLogoutHandler;
import com.socialapp.main.Security.UserActivityFilter;
import com.socialapp.main.Security.Acl.MyAclAuthorizationStrategyImpl;
import com.socialapp.main.Security.Acl.MyAclPermissionEvaluator;
import com.socialapp.main.Security.Acl.MyBasicLookupStrategy;
import com.socialapp.main.Security.Acl.MyJdbcMutableAclService;
import com.socialapp.main.Security.Acl.MyPermission;
import com.socialapp.main.Security.Acl.MySidRetrievalStrategy;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
	Отключаем PropertyPlaceholderAutoConfiguration чтобы задавать значение по умолчанию, 
	если свойство явно не задано в properties (например, для указания размера для user files,
	если не указано userfile.minsize и/или userfile.maxsize)
	
 */

@SpringBootApplication(exclude = {PropertyPlaceholderAutoConfiguration.class})
@ConfigurationPropertiesScan
@EnableMethodSecurity
public class SocialAppApplication {
	
	@Autowired
	private Logger mainLogger;
	
	@Value("webauthn.rpName")
	private String rpName;
	
	@Value("webauthn.rpId")
	private String rpId;
	
	@Value("webauthn.allowedOrigins")
	private String allowedOrigins;
	
	@Autowired
	private MyJdbcSessionRegistry sessionRegistry;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Value("server.servlet.session.cookie.name")
	private String cookieName;
	
	@Autowired
	private MainRepository repo;
	
	@Autowired
	private DatabaseRequestCache dbRequestCache;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	@Autowired
	private DispatcherServlet dispatcherServlet;

	public static void main(String[] args) {
		
		SpringApplication app = new SpringApplication(SocialAppApplication.class);
		//app.addListeners(e->System.out.println("!!!!!!!!!" + e.getClass()));	
		app.setApplicationStartup(new BufferingApplicationStartup(2048));	
		app.setEnvironmentPrefix("social");
		app.run(args);			
				
	}
	
	@Bean
	ExitCodeGenerator exitCodeGenerator()
	{
		return ()->1;
	}	
	
	
	
	//Задаем Executor для задач @EnableAsync
	@Bean
	Executor taskExecutor()
	{
		ThreadPoolTaskExecutorBuilder builder = new ThreadPoolTaskExecutorBuilder();
		builder.threadNamePrefix("task-executor-");
		builder.corePoolSize(10);
		builder.maxPoolSize(20);
		builder.queueCapacity(100);
		
		return builder.build();
	}
	
	//задаем Executor для Spring MVC, Spring WebFlux, Spring GraphQL, Spring WebSocket и JPA
	@Bean
	AsyncTaskExecutor applicationTaskExecutor()
	{
		ThreadPoolTaskExecutorBuilder builder = new ThreadPoolTaskExecutorBuilder();
		builder.threadNamePrefix("app-executor-");
		builder.corePoolSize(10);
		builder.maxPoolSize(20);
		builder.queueCapacity(100);
		
		return builder.build();
	}
	
	//задаем Executor для загрузчика ApplicationContext
	@Bean
	AsyncTaskExecutor bootstrapExecutor()
	{
		ThreadPoolTaskExecutorBuilder builder = new ThreadPoolTaskExecutorBuilder();
		builder.threadNamePrefix("bootstrap-executor-");
		builder.corePoolSize(10);
		builder.maxPoolSize(20);
		builder.queueCapacity(100);
		
		return builder.build();
	}
	
	//Настраиваем встроенный Tomcat сервер программно
	@Bean
	WebServerFactoryCustomizer<TomcatServletWebServerFactory> webServerFactoryCustomizer()
	{
		return new WebServerFactoryCustomizer<TomcatServletWebServerFactory>() {
			
			@Override
			public void customize(TomcatServletWebServerFactory factory)
			{
				factory.setContextLifecycleListeners(List.of(
						new LifecycleListener()
						{
							@Override
							public void lifecycleEvent(LifecycleEvent event)
							{
								mainLogger.debug(event.getType());								
							}
						}
						
						));
			}
			
		};
	}	
		
			
	@Bean
	public SecurityFilterChain secFilterChain(HttpSecurity http, DataSource dataSource) throws Exception
	{		
		var tokenRepository = new JdbcTokenRepositoryImpl();
		tokenRepository.setDataSource(dataSource);
		
		var clearSiteData = new HeaderWriterLogoutHandler(
				new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.ALL));
		
		return
		http
		.httpBasic(basic->basic.disable())	
		.addFilterAfter(new UserActivityFilter(repo), AuthorizationFilter.class)
		.addFilterAfter(new CheckAuthenticationFilter(sessionRegistry, dataSource, eventPublisher), AuthorizationFilter.class)
		.requestCache(c->c.requestCache(dbRequestCache))
		.formLogin(form->form
				.loginPage(("/"))				
				.loginProcessingUrl("/login")	
				.defaultSuccessUrl("/profile/")				
				.permitAll()				
				)				
		.webAuthn(webAuthn->webAuthn				
				.rpName("SocialApp")
				.rpId("localhost")
				.allowedOrigins("http://localhost:8080/")	
				.disableDefaultRegistrationPage(true)				
				)		
		.oneTimeTokenLogin(ott->ott
				.loginPage("/")
				.loginProcessingUrl("/")
				)		
		.logout(logout->logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/")	
				.addLogoutHandler(clearSiteData)	
				.addLogoutHandler(new MyJdbcSessionRegistryLogoutHandler(sessionRegistry))
				.permitAll()	
				)
		.rememberMe(rm->rm
				.key("remember-me-key-12$%34")
				.tokenRepository(tokenRepository)
				)
		.authorizeHttpRequests(req->req					
				.requestMatchers("/").permitAll()
				.requestMatchers("/reg/**").permitAll()				
				.requestMatchers("/style.css").permitAll()
				.requestMatchers("/mywebauthn.js").permitAll()	
				.requestMatchers("/error").permitAll()				
				.requestMatchers("/admin/**").hasRole("ADMIN")	
				//.anyRequest().permitAll()
				.anyRequest().authenticated()
				)
		.sessionManagement(s->s
				.invalidSessionUrl("/")				
				.maximumSessions(2)					
				.sessionRegistry(sessionRegistry)		
				.expiredUrl("/")
				)		
		.build();
	}
	
	@Bean
	public static RoleHierarchy roleHierarchy() {
		return
		RoleHierarchyImpl.withDefaultRolePrefix()
		.role("ADMIN").implies("USER")
		.build();
	}
	
	// по умолчанию недоступен как bean, поэтому объявляем явно для последующего внедрения
	@Bean
	public static GrantedAuthorityDefaults grantedAuthorityDefaults() {
		return new GrantedAuthorityDefaults("ROLE_");
	}
	
	@Bean
	public static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setRoleHierarchy(roleHierarchy);
		return expressionHandler;
	}	
	
	@Bean
	OneTimeTokenGenerationSuccessHandler handler()
	{		
		return new OneTimeTokenGenerationSuccessHandler() 
		{		
			@Override
			public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken) throws IOException, ServletException {
				// ничего не делает, т.к. в этом нет смысла
			}
		};			
	}

	
	@Bean
	public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, OneTimeTokenService oneTimeTokenService)
	{
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);	
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
		
		OneTimeTokenAuthenticationProvider oneTimeTokenProvider = 
				new OneTimeTokenAuthenticationProvider(oneTimeTokenService, userDetailsService);
		
		return new ProviderManager(daoAuthenticationProvider, oneTimeTokenProvider);
	}	
	
	
	//Настраиваем Passkeys
	@Bean
	JdbcPublicKeyCredentialUserEntityRepository jdbcPublicKeyCredentialRepository(JdbcOperations jdbc) {
		return new JdbcPublicKeyCredentialUserEntityRepository(jdbc);
	}

	@Bean
	JdbcUserCredentialRepository jdbcUserCredentialRepository(JdbcOperations jdbc) {
		return new JdbcUserCredentialRepository(jdbc);
	}
	
	//Настраиваем One-Time Token
	@Bean
	JdbcOneTimeTokenService jdbcOneTimeTokenService(JdbcOperations jdbc)
	{
		return new JdbcOneTimeTokenService(jdbc);
	}
	
	//Используется для управления параллельными сеансами
	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
	    return new HttpSessionEventPublisher();
	}
	
	//Используется для публикации событий аутентификации
	@Bean
	public AuthenticationEventPublisher authenticationEventPublisher
	        (ApplicationEventPublisher applicationEventPublisher) {
	    return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
	}
	
	//Используется для публикации событий авторизации
	@Bean
	public AuthorizationEventPublisher authorizationEventPublisher
	        (ApplicationEventPublisher applicationEventPublisher) {
	    return new SpringAuthorizationEventPublisher(applicationEventPublisher);
	}
	
	//Настраиваем Acl
	@Bean
	static MethodSecurityExpressionHandler expressionHandler(AclPermissionEvaluator aclPermissionEvaluator) {
		final DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setPermissionEvaluator(aclPermissionEvaluator);
		return expressionHandler;
	}

	@Bean
	static AclPermissionEvaluator aclPermissionEvaluator(AclService aclService) {
		var evaluator = new MyAclPermissionEvaluator(aclService);
		evaluator.setSidRetrievalStrategy(new MySidRetrievalStrategy());
		evaluator.setPermissionFactory(new DefaultPermissionFactory(MyPermission.class));
		
		return evaluator;
	}

	@Bean
	static JdbcMutableAclService aclService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
		
		var service =  new MyJdbcMutableAclService(dataSource, lookupStrategy, aclCache);
		
		service.setClassIdentityQuery("select currval(pg_get_serial_sequence('acl_class', 'id'))");
		service.setSidIdentityQuery("select currval(pg_get_serial_sequence('acl_sid', 'id'))");
		
		return service;		
	}

	@Bean
	static LookupStrategy lookupStrategy(DataSource dataSource, AclCache cache,
			AclAuthorizationStrategy aclAuthorizationStrategy, PermissionGrantingStrategy permissionGrantingStrategy) {
		var lookupStrategy =  new MyBasicLookupStrategy(dataSource, cache, aclAuthorizationStrategy, permissionGrantingStrategy);
		lookupStrategy.setPermissionFactory(new DefaultPermissionFactory(MyPermission.class));
		lookupStrategy.setAclClassIdSupported(true);
		return lookupStrategy;
	}

	@Bean
	static AclCache aclCache(PermissionGrantingStrategy permissionGrantingStrategy,
			AclAuthorizationStrategy aclAuthorizationStrategy) {
		Cache cache = new ConcurrentMapCache("aclCache");
		return new SpringCacheBasedAclCache(cache, permissionGrantingStrategy, aclAuthorizationStrategy);
	}

	@Bean
	static AclAuthorizationStrategy aclAuthorizationStrategy() {
		return new MyAclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ADMIN"));
	}

	@Bean
	static PermissionGrantingStrategy permissionGrantingStrategy() {
		return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
	}

		
}
