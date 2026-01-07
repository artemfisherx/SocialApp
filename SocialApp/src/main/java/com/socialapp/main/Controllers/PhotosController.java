package com.socialapp.main.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.socialapp.main.Annotations.FileSize;
import com.socialapp.main.Security.UserResolver;
import com.socialapp.main.Services.UserService;

@Controller
@RequestMapping("/photos")
@PreAuthorize("hasRole('USER')")
public class PhotosController {
	
	@Autowired
	private UserResolver userIdResolver;	
	
	@Autowired
	private UserService userService;
	
	@GetMapping("/photos")
	String getPhotos(Model model)
	{
		int userId = userIdResolver.getId();
		var photos = userService.getPhotos(userId);
		model.addAttribute("photos", photos);	
				
		return "photos";
	}
	
	@PostMapping("/addphotos")
	String addPhotos(@RequestPart("files") List<@FileSize MultipartFile> files)
	{		
		int owner = userIdResolver.getId();
		
		userService.savePhotos(files, owner);
		
		return "redirect:/photos";
	}
	
	@ResponseBody
	@PostMapping("/deletephoto")
	void deletePhoto(int id)
	{
		int userId = userIdResolver.getId();
		userService.deletePhoto(userId, id);
	}	

}
