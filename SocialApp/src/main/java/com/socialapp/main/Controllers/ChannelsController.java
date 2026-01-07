package com.socialapp.main.Controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.socialapp.main.Entities.Channel;
import com.socialapp.main.Entities.WallMessageIn;
import com.socialapp.main.Entities.WallMessageOut;
import com.socialapp.main.Security.UserResolver;
import com.socialapp.main.Services.ChannelService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/channels")
@PreAuthorize("hasRole('USER')")
public class ChannelsController {
	
	private Logger logger = LoggerFactory.getLogger(ChannelsController.class);
	
	@Autowired
	private ChannelService channelService;
	
	@Autowired
	private UserResolver userResolver;
	
	// CHANNELS
	
	@GetMapping
	public String getChannels(Model model)
	{
		int userId = userResolver.getId();		
		List<Channel> channelsOfUser = channelService.getChannelsOfUser(userId);		
		model.addAttribute("channelsOfUser", channelsOfUser);
		
		List<Channel> channelsOfNotUser = channelService.getChannelsOfNotUser(userId);		
		model.addAttribute("channelsOfNotUser", channelsOfNotUser);
			
		return "channels";
	}
		
	@GetMapping("/create")
	String getCreateChannel(Model model)
	{
		Channel channel = new Channel();
		model.addAttribute("channel", channel);
			
		return "createChannel";
	}
		
	@PostMapping("/create")
	String postChannel(@Valid Channel channel, BindingResult result, Model model)
	{
		if(result.hasErrors())
		{
			model.addAttribute("channel", channel);			
			return "createChannel";
		}
			
		int userId = userResolver.getId();
			
		int id = channelService.saveChannel(channel, userId);
			
		return "redirect:/channels/" + id;
	}
		
	@GetMapping("/{id:\\d+}")
	String getChannel(@PathVariable("id") int id, Model model)
	{
		Channel channel = channelService.getChannel(id);
		model.addAttribute("channel", channel);
			
		WallMessageIn message = new WallMessageIn();
		message.setReceiver(id);
		model.addAttribute("message", message);
			
		var wallMessages = channelService.getChannelWallMessages(id);		
		model.addAttribute("wallMessages", wallMessages);
		
		model.addAttribute("channelId", id);
		
		int userId = userResolver.getId();		
		model.addAttribute("userId", userId);
		
		boolean isSubscriber = userResolver.isSubscriber(id);
		model.addAttribute("isSubscriber", isSubscriber);
			
		return "channel";
	}
	
	@ResponseBody
	@PostMapping("removechannel")
	public void postRemoveChannel(int channelId) {
		
		int userId = userResolver.getId();		
		channelService.deleteChannel(userId, channelId);		
	}
		
	@ResponseBody
	@PostMapping("/postchannelwallmessage")	
	WallMessageOut postChannelWallMessage(WallMessageIn message)
	{
		System.out.println("WallMessageIn text:" + message.getText());
		System.out.println("WallMessageIn image:" + message.getImage());
			
		//заглушка в целях разработки
		int userId = userResolver.getId();
		message.setSender(userId);		
			
		int length = message.getText().length();
		
		if(message.getImage()==null&&length==0)
			return null;
			
		return channelService.saveChannelWallMessage(message);
	}
		
	@ResponseBody
	@PostMapping("/deletechannelwallmessage")	
	void deleteChannelWallMessage(int id, int channelId)
	{
		if(userResolver.isChannelOwner(channelId))
			channelService.deleteChannelWallMessage(id);
		else
		{
			int userId = userResolver.getId();
			channelService.deleteChannelWallMessage(id, userId);
		}		
	}
			
	@PostMapping("/subscribe")	
	@ResponseBody
	void postSubscribe(int channelId)
	{
		int userId = userResolver.getId();
		channelService.subscribe(channelId, userId);		
	}
		
	@PostMapping("/unsubscribe")
	@ResponseBody
	void postUnsubscribe(int channelId)
	{
		int userId = userResolver.getId();
		channelService.unsubscribe(channelId, userId);		
	}

}
