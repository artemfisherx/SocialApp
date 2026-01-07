package com.socialapp.main.Services;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.method.HandleAuthorizationDenied;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import com.socialapp.main.FileUtils;
import com.socialapp.main.MainRepository;
import com.socialapp.main.Entities.Channel;
import com.socialapp.main.Entities.Profile;
import com.socialapp.main.Entities.WallMessageIn;
import com.socialapp.main.Entities.WallMessageOut;
import com.socialapp.main.Security.MyMethodAuthorizationDeniedHandler;

@Service
public class ChannelService {
	
	private Logger logger = LoggerFactory.getLogger(ChannelService.class);
	
	@Autowired
	private MainRepository repo;
	
	@Value("${userfile.dir}")
	private String userFileDir;
	
	public List<Channel> getChannelsOfUser(int userId)
	{
		return repo.selectChannelsOfUser(userId);
	}
	
	@PostFilter("filterObject.owner.id!=#userId")
	public List<Channel> getChannelsOfNotUser(@P("userId") int userId)
	{
		return repo.selectChannelsOfNotUser(userId);
	}
	
	@PreAuthorize("@userResolver.getId()==#userId")
	public int saveChannel(Channel channel, @P("userId") int userId)
	{
		return repo.insertChannel(channel, userId);
	}
	
	public Channel getChannel(int id)
	{
		return repo.selectChannel(id);
	}
	
	@PreAuthorize("@channelService.getChannel(#channelId).owner.id==#userId") //проверяем что канал удаляет его владелец
	@HandleAuthorizationDenied(handlerClass=MyMethodAuthorizationDeniedHandler.class)
	public void deleteChannel(@P("userId") int userId, @P("channelId") int channelId)
	{
		repo.deleteChannel(channelId);
	}
	
	public WallMessageOut saveChannelWallMessage(WallMessageIn messageIn)
	{
		String filename = "";
		if(messageIn.getImage()!=null)
			filename = FileUtils.getNewFilename(messageIn.getImage().getOriginalFilename());		
		
		var dt = OffsetDateTime.now();
		messageIn.setDt(dt);
		
		int id = repo.insertChannelWallMessage(messageIn, filename);				
		
		if(messageIn.getImage()!=null)
			FileUtils.saveFile(messageIn.getImage(), userFileDir, filename);
		
		Profile profile = repo.selectProfile(messageIn.getSender());
		
		var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");		
		
		var messageOut = new WallMessageOut();
		messageOut.setId(id);
		messageOut.setText(messageIn.getText());
		messageOut.setImagePath(filename);
		messageOut.setSenderId(messageIn.getSender());
		messageOut.setSenderName(profile.getName() + " " + profile.getSurname());
		messageOut.setDt(dt.format(formatter));			
		
		return messageOut;	
	}
	
	public List<WallMessageOut> getChannelWallMessages(int id)
	{
		return repo.selectChannelWallMessages(id);
	}
	
	public void deleteChannelWallMessage(int id)
	{
		repo.deleteChannelWallMessage(id);
	}
	
	public void deleteChannelWallMessage(int id, int userId)
	{
		repo.deleteChannelWallMessage(id, userId);
	}
	
	
	@PreAuthorize("(!@userResolver.isChannelOwner(#channelId))&&(!@userResolver.isSubscriber(#channelId))&&"
			+ "@userResolver.getId()==#userId")
	@HandleAuthorizationDenied(handlerClass=MyMethodAuthorizationDeniedHandler.class)
	public void subscribe(@P("channelId") int channelId, @P("userId") int userId)
	{
		repo.insertSubscriber(channelId, userId);
	}
	
	@PreAuthorize("(!@userResolver.isChannelOwner(#channelId))&&(@userResolver.isSubscriber(#channelId))&&"
			+ "@userResolver.getId()==#userId")
	@HandleAuthorizationDenied(handlerClass=MyMethodAuthorizationDeniedHandler.class)
	public void unsubscribe(int channelId, int userId)
	{
		repo.deleteSubscriber(channelId, userId);
	}	

}
