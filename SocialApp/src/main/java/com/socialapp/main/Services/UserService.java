package com.socialapp.main.Services;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.AccessControlEntryImpl;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.socialapp.main.FileUtils;
import com.socialapp.main.MainRepository;
import com.socialapp.main.Entities.Photo;
import com.socialapp.main.Entities.Privacy;
import com.socialapp.main.Entities.Profile;
import com.socialapp.main.Entities.RegForm;
import com.socialapp.main.Entities.WallMessageIn;
import com.socialapp.main.Entities.WallMessageOut;
import com.socialapp.main.Enums.MaritalStatus;
import com.socialapp.main.Security.UserResolver;
import com.socialapp.main.Security.Acl.MyJdbcMutableAclService;
import com.socialapp.main.Security.Acl.MyPermission;
import com.socialapp.main.Security.Acl.MyPrincipalSid;

@Service
@Transactional
public class UserService {
	
	private Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private MainRepository repo;
	
	@Value("${userfile.dir}")
	private String userFileDir;
	
	@Autowired
	private UserResolver userResolver;
	
	@Autowired
	private MyJdbcMutableAclService aclService;
	
		
	public int createUser(RegForm regForm)
	{			
		String origFilename = regForm.getImage().getOriginalFilename();
		String filename = FileUtils.getNewFilename(origFilename);	
		
		int id = repo.insertUser(regForm, filename);		
		
		FileUtils.saveFile(regForm.getImage(), userFileDir, filename);	
		
		setAclOwner(id);
		
		return id;
	}
	
	//Создаем пользователя для входа без регистрации
	public String createTempUser()
	{
		RegForm user = new RegForm();
		String login = "temp" + UUID.randomUUID().toString();
		
		user.setLogin(login);
		user.setPassword(login);		
		user.setName("Alexander");
		user.setSurname("Pushkin");
		user.setBirthDate(LocalDate.of(1799, 1 , 6));
		user.setMaritalStatus(MaritalStatus.Not_Married);
		user.setPhone("8-900-000-00-00");
		user.setCountry("Russia");
		user.setCity("Saint-Petersburg");
		user.setAbout("Russian poet, playwright and novelist");
		
		String filename = "pushkin.jpg";
		
		int id = repo.insertUser(user, filename);
		
		setAclOwner(id);
		
		return login;
	}
	
	public void blockUser(int profileId, int blockedUserId)
	{
		ObjectIdentity oi = new ObjectIdentityImpl(Profile.class, profileId);
		Sid sid = new MyPrincipalSid(blockedUserId);
		Permission p = MyPermission.VIEW_PROFILE;
		
		MutableAcl acl = null;
		
		try 
		{
			acl = (MutableAcl) aclService.readAclById(oi);
			List<AccessControlEntry> aces = acl.getEntries();
			
			for(int i=0; i<aces.size(); i++)
			{
				AccessControlEntry ace = aces.get(i);
							
				if(ace.getSid().equals(sid)&&ace.getPermission().equals(p))
				{						
					acl.deleteAce(i);
					acl.insertAce(i, p, sid, false);					
				}
				
			}
		}
		catch (NotFoundException ex) 
		{
			acl = aclService.createAcl(oi);
			acl.insertAce(acl.getEntries().size(), p, sid, false);
		}		
		
		aclService.updateAcl(acl);		
	}
	
	public void unblockUser(int userId)
	{
		int profileId = userResolver.getId();
		ObjectIdentity oi = new ObjectIdentityImpl(Profile.class, profileId);
		Sid sid = new MyPrincipalSid(userId);
		Permission p = MyPermission.VIEW_PROFILE;
		
		MutableAcl acl = (MutableAcl) aclService.readAclById(oi);
		List<AccessControlEntry> aces = acl.getEntries();
		
		for(int i=0; i<aces.size(); i++)
		{
			AccessControlEntry ace = aces.get(i);
						
			if(ace.getSid().equals(sid)&&ace.getPermission().equals(p))
			{					
				acl.deleteAce(i);
				acl.insertAce(i, p, sid, true);
				aclService.updateAcl(acl);
			}
			
		}
		
	}
	
	private void setAclOwner(int id)
	{
		ObjectIdentity oi = new ObjectIdentityImpl(Profile.class, id);
		MutableAcl acl = aclService.createAcl(oi);
		acl.setOwner(new MyPrincipalSid(id));
		aclService.updateAcl(acl);		
	}	
	
		
	@PostAuthorize("hasPermission(returnObject,  'view_profile')")	
	public Profile getProfile(int id)
	{
		return repo.selectProfile(id);		
	}
	
	@PreAuthorize("@userResolver.getId()==#userId")
	public void updateLogin(@P("userId")int userId, String login)
	{
		repo.updateLogin(userId, login);
	}
	
	public Privacy getPrivacy(int userId)
	{
		return repo.selectPrivacy(userId);
	}
	
	public void updatePrivacy(int userId, Privacy privacy)
	{
		repo.updatePrivacy(userId, privacy);
	}
	
	public WallMessageOut saveWallMessage(WallMessageIn messageIn)	
	{
		logger.debug("saveWallMessage start");
		
		String filename = "";
		if(messageIn.getImage()!=null)
			filename = FileUtils.getNewFilename(messageIn.getImage().getOriginalFilename());		
		
		var dt = OffsetDateTime.now();
		messageIn.setDt(dt);
		
		int id = repo.insertWallMessage(messageIn, filename);				
		
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
		
		logger.debug("saveWallMessage success finish");
		
		return messageOut;	
	}
	
	@PreAuthorize("hasRole('USER')")
	public List<WallMessageOut> getWallMessages(int id)
	{
		return repo.selectWallMessages(id);
	}
	
	@PreAuthorize("hasRole('USER')")
	public void deleteWallMessage(int id)
	{
		repo.deleteWallMessage(id);
	}
	
	@PreAuthorize("#owner==@userResolver.getId()")
	public void savePhotos(List<MultipartFile> files, @P("owner") int owner)
	{
		int size = files.size();		
		
		for(int i=0; i<size; i++)
		{
			MultipartFile file = files.get(i);
			String filename = FileUtils.getNewFilename(file.getOriginalFilename());			
			repo.insertPhotos(filename, owner);
			FileUtils.saveFile(file, userFileDir, filename);
		}		
	}
	
	public List<Photo> getPhotos(int user)
	{
		return repo.getPhotos(user);
	}
	
	@PreAuthorize("@userResolver.getId()==#userId || hasRole('ADMIN')")
	public void deletePhoto(@P("userId") int userId, int id)
	{
		String filename = repo.deletePhoto(userId, id);
		FileUtils.deleteFile(userFileDir, filename);
		
	}
	
	public void saveFRequest(int userFrom, int userTo)
	{
		if(!isFriend(userFrom, userTo))
			repo.saveFRequest(userFrom, userTo);
	}
	
	public Map<String, Object> getFRequest(int user1, int user2)
	{
		return repo.selectFriendRequest(user1, user2);
	}	
	
	public List<FriendRequest> getFRequestForUser(int userId)
	{
		 List<Map<String, Object>> rows =  repo.selectFriendRequestForUser(userId);
		 List<FriendRequest> requests = new ArrayList<>();
		 
		 for(Map<String, Object> row : rows)
		 {
			 int fromId = (int)row.get("user_from");
			 int toId = (int)row.get("user_to");
			 String fromName = this.getProfile(fromId).getName() + " " + this.getProfile(fromId).getSurname();
			 String toName = this.getProfile(toId).getName() + " " + this.getProfile(toId).getSurname();
			 
			 requests.add(new FriendRequest(fromId, toId, fromName, toName));			 
		 }
		 
		 return requests;
	}
		
	public boolean isFriend(int user1, int user2)
	{
		return repo.isFriend(user1, user2);
	}
	
	public List<Friend> getFriendsForUser(int userId)
	{
		List<Map<String, Object>> rows = repo.getFriendsForUser(userId);
		List<Friend> friends = new ArrayList<>();
		
		for(Map<String, Object> row:rows)
		{
			int user1 = (int)row.get("user1");
			int user2 = (int)row.get("user2");
			
			if(userId==user1) // значит friend = user2
			{
				String name = this.getProfile(user2).getName();
				String surname = this.getProfile(user2).getSurname();
				friends.add(new Friend(user2, name + " " + surname));
			}
			
			if(userId==user2) // значит friend = user1
			{
				String name = this.getProfile(user1).getName();
				String surname = this.getProfile(user1).getSurname();
				friends.add(new Friend(user1, name + " " + surname));
			}
		}
		
		return friends;
		
	}
	
	public void cancelFRequest(int user1, int user2)
	{
		repo.deleteFRequest(user1, user2);
	}
	
	public void acceptFRequest(int user1, int user2)
	{
		if(!isFriend(user1, user2))
			repo.insertFriend(user1, user2);
	}
	
	public void deleteFriend(int user1, int user2)
	{
		repo.deleteFriend(user1, user2);
	}
	
	public List<Profile> getBlockedUsers(int ownerId)
	{
		return repo.selectBlockedUsers(ownerId);
	}
	
	public static class FriendRequest{
		
		private int fromId;
		private int toId;
		private String fromName;
		private String toName;
		
		public FriendRequest(int fromId, int toId, String fromName, String toName)
		{
			this.fromId = fromId;
			this.toId = toId;
			this.fromName = fromName;
			this.toName = toName;
		}
		
		public int getFromId() {
			return fromId;
		}
		public void setFromId(int fromId) {
			this.fromId = fromId;
		}
		public int getToId() {
			return toId;
		}
		public void setToId(int toId) {
			this.toId = toId;
		}
		public String getFromName() {
			return fromName;
		}
		public void setFromName(String fromName) {
			this.fromName = fromName;
		}
		public String getToName() {
			return toName;
		}
		public void setToName(String toName) {
			this.toName = toName;
		}
		
	}
	
	public static class Friend {
		
		private int id;
		private String name;
		
		public Friend(int id, String name)
		{
			this.id = id;
			this.name = name;
		}
		
		public int getId()
		{
			return id;
		}
		
		public String getName()
		{
			return name;
		}
		
	}
}
