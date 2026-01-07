package com.socialapp.main.Controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin")
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
	
	@GetMapping
	public String getAdmin()
	{
		return "admin";
	}

}
