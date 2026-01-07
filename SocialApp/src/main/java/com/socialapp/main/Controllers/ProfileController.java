package com.socialapp.main.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.socialapp.main.ConfigurationProperties.ConfigProperties;
import com.socialapp.main.Entities.WallMessageIn;
import com.socialapp.main.Entities.WallMessageOut;
import com.socialapp.main.Security.UserResolver;
import com.socialapp.main.Services.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/profile")
public class ProfileController {	
		
	private Logger logger = LoggerFactory.getLogger(ProfileController.class);
	
	@Autowired
	private ConfigProperties props;
	
	@Autowired
	private UserService userService;	

	@Autowired
	private UserResolver userResolver;	
	
	//Для авторизованного пользователя
	//добавил этот метод, потому что метод getIndex не срабатывает 
	//даже при @PathVariable(value="id", required=false) Integer id
	
	
	@GetMapping
	String getProfile(HttpSession session)
	{	
		int id = userResolver.getId();		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();	
		logger.debug(auth.toString());
		
		return "redirect:/profile/" + id;
	}	
	
	
	@GetMapping("/{id:\\d}")
	String getIndex(@PathVariable(value="id") Integer id, 
			@RequestParam(value="continue", required=false) String con, Model model) throws Exception
	{		
		
		//if(id==null)
		//	id = userResolver.getId();
		
		var profile = userService.getProfile(id);
		List<WallMessageOut> wallMessages = userService.getWallMessages(id);
		
		if(wallMessages==null)
			wallMessages = new ArrayList<>();
		
		int authenticated = userResolver.getId();
		
		boolean fRequestExists = false; //существует ли заявка в друзья	
		boolean isFriend = false; //являются ли пользователя друзьями
		int from = 0; // отправитель заявки в друзья
		int to = 0; // кому отправлена заявка
		if(id!=authenticated)
		{
			Map<String, Object> fRequest = userService.getFRequest(authenticated, profile.getId());
			isFriend = userService.isFriend(authenticated, profile.getId());
			
			if(fRequest!=null)
			{
				fRequestExists = true;
				from = (int)fRequest.getOrDefault("user_from", 0);
				to = (int)fRequest.getOrDefault("user_to", 0);
			}
		}		
		
		var wallMessageIn = new WallMessageIn();
		wallMessageIn.setReceiver(id);
		
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("fRequestExists", fRequestExists);
		model.addAttribute("isFriend", isFriend);
		model.addAttribute("authenticated", authenticated);
		model.addAttribute("profile", profile);
		model.addAttribute("wallMessages", wallMessages);
		model.addAttribute("message", wallMessageIn);	
		
		
		return "profile";
	}
	
	@ResponseBody
	@PostMapping("/postwallmessage")
	WallMessageOut postWallMessage(WallMessageIn message)
	{		
		
		logger.debug("postWallMessage message receiver:" + message.getReceiver());
		
		int authenticated = userResolver.getId();
		message.setSender(authenticated);		
		
		int length = message.getText().length();
		
		if(message.getImage()==null&&length==0)
			return null;
		
		return userService.saveWallMessage(message);
	}
	
	@ResponseBody
	@PostMapping("/deletewallmessage")
	@PreAuthorize("@userResolver.getId()==#senderId")	
	void deleteWallMessage(@P("senderId") int senderId, int messageId)
	{		
		userService.deleteWallMessage(messageId);
	}
		
	
	//Сохряняем заявку на добавление в друзья
	@ResponseBody
	@PostMapping("/frequest")
	void postFRequest(int to)
	{
		int from = userResolver.getId();	
		userService.saveFRequest(from, to);		
	}
	
	//Удаляем заявку на добавление в друзья
	@ResponseBody
	@PostMapping("/cancelfrequest")
	void postCancelFRequest(int to)
	{
		int from = userResolver.getId();	
		userService.cancelFRequest(from, to);
	}
	
	//Принимаем заявку на добавление в друзья
	@ResponseBody
	@PostMapping("/acceptfrequest")
	void postAcceptFRequest(int from)
	{		
		int to = userResolver.getId();		
		userService.acceptFRequest(from, to);
	}	
	
	
	//Удаляем из друзей
	@ResponseBody
	@PostMapping("/deletefriend")
	void deleteFriend(int friend)
	{
		int user = userResolver.getId();	
		userService.deleteFriend(user, friend);
	}
	
	@ResponseBody
	@PostMapping("/blockuser")
	public void postBlockUser(int id)
	{
		int profileId = userResolver.getId();
		userService.blockUser(profileId, id);
	}
	
	@ResponseBody
	@PostMapping("/unblockuser")
	public void unpostBlockUser(int id)
	{
		
	}

}
