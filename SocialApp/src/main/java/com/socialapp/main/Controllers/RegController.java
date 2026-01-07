package com.socialapp.main.Controllers;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.JdbcOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.socialapp.main.Entities.RegForm;
import com.socialapp.main.Entities.RegForm.RegisterStep1;
import com.socialapp.main.Entities.RegForm.RegisterStep2;
import com.socialapp.main.Services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/reg")
@PreAuthorize("principal.equals('anonymousUser')")
public class RegController {
	
	@Autowired
	private UserService userService;	
	
	@Autowired
	private AuthenticationManager authManager;	
	
	@Autowired
	private JdbcOneTimeTokenService tokenService;
	
	//Для неавторизованного пользователя
	
		/*
		@GetMapping("/login")
		String getLogin(Model model)
		{	
			return "index";
		}
		*/	
			
		@GetMapping("/register1")
		String getRegister1(Model model, HttpSession session)
		{
			
			//удаляем отметку, что первый шаг регистрации пройден. Тем самым понимаем, что новая регистрация
			session.removeAttribute("register1"); 
			
			model.addAttribute("regForm", new RegForm());
			return "unsecured/register1";
		}
		
		@PostMapping("/register1")
		String postRegister1(@Validated(RegisterStep1.class) RegForm regForm, BindingResult result, Model model,
				HttpSession session)
		{	
			
			if(result.hasErrors())
			{
				model.addAttribute("regForm", regForm);
				return "unsecured/register1";
			}
			
			session.setAttribute("regForm", regForm);
			session.setAttribute("register1", true); //запоминаем, что первый шаг регистрации пройден
			
			return "redirect:/reg/register2";
		}
		
		@GetMapping("/register2")
		String getRegister2(Model model, HttpSession session)
		{	
			//проверяем пройден ли первый шаг регистрации
			if(session.getAttribute("register1")==null)
				return "redirect:/reg/register1";
			
			model.addAttribute("regForm", new RegForm());
			return "unsecured/register2";
		}
		
		@PostMapping("/register2")
		String postRegister2(@Validated(RegisterStep2.class) RegForm regForm, BindingResult result, Model model,
				HttpSession session, HttpServletRequest request, HttpServletResponse response)
		{		
			if(result.hasErrors())
			{
				model.addAttribute("regForm", regForm);
				return "unsecured/register2";
			}
			
			RegForm regFormTotal = (RegForm) session.getAttribute("regForm");
			
			if(regFormTotal==null)
				return "redirect:/reg/register1";
			
			regFormTotal.setPhone(regForm.getPhone());		
			regFormTotal.setCountry(regForm.getCountry());
			regFormTotal.setCity(regForm.getCity());
			regFormTotal.setAbout(regForm.getAbout());
			regFormTotal.setImage(regForm.getImage());		
			
			
			int id = userService.createUser(regFormTotal);	
			
			//удаляем отметку, что первый шаг регистрации пройден, и объект regForm. Чтобы не хранить их в сессии зря
			session.removeAttribute("register1"); 
			session.removeAttribute("regForm");
			
			//делаем аутентификацию зарегистрированнного пользователя
			var token = new UsernamePasswordAuthenticationToken(regFormTotal.getLogin(), regFormTotal.getPassword());
			var authentication = authManager.authenticate(token);			
			var strategy = SecurityContextHolder.getContextHolderStrategy();
			SecurityContext context = strategy.createEmptyContext();
			context.setAuthentication(authentication);
			strategy.setContext(context);
			
			
			SecurityContextRepository secRepo = new HttpSessionSecurityContextRepository();
			secRepo.saveContext(context, request, response);
			
			return "redirect:/profile/";
		}
		
		//Создаем пользователя для входа без регистрации
		@PostMapping("/temp")		
		String postTemp(HttpServletRequest request, HttpServletResponse response)
		{
			String username = userService.createTempUser();
			
			//Создаем запрос на токен
			var tokenRequest = new GenerateOneTimeTokenRequest(username, Duration.ofMinutes(1));			
			//генерируем токен
			OneTimeToken token = tokenService.generate(tokenRequest);
			
			//аутентифицируем по токену
			var authToken = new OneTimeTokenAuthenticationToken(token.getTokenValue());			
			var authentication = authManager.authenticate(authToken);			
			var strategy = SecurityContextHolder.getContextHolderStrategy();
			SecurityContext context = strategy.createEmptyContext();
			context.setAuthentication(authentication);
			strategy.setContext(context);				
				
			SecurityContextRepository secRepo = new HttpSessionSecurityContextRepository();
			secRepo.saveContext(context, request, response);
			
			return "redirect:/profile/";
		}		
}
