package com.socialapp.main.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.socialapp.main.Security.UserResolver;
import com.socialapp.main.Services.UserService;
import com.socialapp.main.Services.UserService.Friend;
import com.socialapp.main.Services.UserService.FriendRequest;

@RequestMapping("/friends")
@Controller
@PreAuthorize("hasRole('USER')")
public class FriendsController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserResolver userResolver;
	
	@GetMapping
	public String getFriends(Model model)
	{
		int userId = userResolver.getId();
		List<FriendRequest> requests = userService.getFRequestForUser(userId);		
		List<Friend> friends = userService.getFriendsForUser(userId);
		
		model.addAttribute("userId", userId);
		model.addAttribute("requests", requests);
		model.addAttribute("friends", friends);
		
		return "friends";
	}

}
