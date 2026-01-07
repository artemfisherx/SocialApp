package com.socialapp.main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.socialapp.main.ConfigurationProperties.ServerServletSessionProperties;
import com.socialapp.main.Entities.Channel;
import com.socialapp.main.Entities.Photo;
import com.socialapp.main.Entities.Privacy;
import com.socialapp.main.Entities.Profile;
import com.socialapp.main.Entities.RegForm;
import com.socialapp.main.Entities.WallMessageIn;
import com.socialapp.main.Entities.WallMessageOut;
import com.socialapp.main.Enums.MaritalStatus;
import com.socialapp.main.Enums.PrivacyType;
import com.socialapp.main.Security.MySessionInformation;
import com.socialapp.main.Security.MyUserDetails;
import com.socialapp.main.Security.Acl.MyPermission;

@Repository
public class MainRepository {
	
	@Autowired
	private JdbcClient client;	
	
	@Autowired
	private PasswordEncoder encoder;
	
	@Autowired
	private ServerServletSessionProperties props;
	
	@Autowired
	private GrantedAuthorityDefaults grantedAuthorityDefaults; 
	
	private Logger logger = LoggerFactory.getLogger(MainRepository.class);
	
	public int insertUser(RegForm regForm, String filename)
	{
		KeyHolder keyHolder = new GeneratedKeyHolder();	
		
		String pass = regForm.getPassword().trim();
		String password = encoder.encode(pass);
		
		client.sql("INSERT INTO users(login, password) VALUES(:login, :password)")
				.param("login", regForm.getLogin().trim())
				.param("password", password)
				.update(keyHolder, "id");
		
		int id = keyHolder.getKey().intValue();	
		
		insertProfile(regForm, id, filename);
		insertPrivacy(id, new Privacy());		
		
		return id;
		
	}
	
	private void insertProfile(RegForm regForm, int user_id, String filename)
	{
		client.sql("INSERT INTO profiles(user_id, name, surname, birth_date, marital_status, "
				+ "phone, country, city, about, image) "
				+ "VALUES(:user_id, :name, :surname, :birth_date, :marital_status,"
				+ ":phone, :country, :city, :about, :image)")
		.param("user_id", user_id)
		.param("name", regForm.getName().trim())
		.param("surname", regForm.getSurname().trim())
		.param("birth_date", regForm.getBirthDate())
		.param("marital_status", regForm.getMaritalStatus().toString().trim())
		.param("phone", regForm.getPhone().trim())
		.param("country", regForm.getCountry().trim())
		.param("city", regForm.getCity().trim())
		.param("about", regForm.getAbout().trim())
		.param("image", filename)
		.update();
	}
	
	public Profile selectProfile(int id)
	{
		return client.sql("SELECT * FROM profiles WHERE user_id=:user_id")
				.param("user_id", id)
				.query(new ProfileRowMapper())
				.single();
	}
	
	public void saveFRequest(int userFrom, int userTo)
	{		
		client.sql("INSERT INTO friend_request(user_from, user_to) VALUES(:userFrom, :userTo)")
		.param("userFrom", userFrom)
		.param("userTo", userTo)
		.update();
	}
	
	public Map<String, Object> selectFriendRequest(int user1, int user2)
	{
		var list = client
		.sql("SELECT user_from, user_to FROM friend_request WHERE (user_from=:user1 AND user_to=:user2)"
				+ " OR (user_from=:user2 AND user_to=:user1) LIMIT 1")
		.param("user1", user1)
		.param("user2", user2)
		.query()
		.listOfRows();
		
		if(list.size()>0)
			return list.get(0);
		
		return null;
		
	}
	
	public List<Map<String, Object>> selectFriendRequestForUser(int userId)
	{
		return
		client
		.sql("SELECT user_from, user_to FROM friend_request WHERE user_from=:userId"
				+ " OR user_to=:userId")
		.param("userId", userId)		
		.query()
		.listOfRows();
	}
	
	public boolean isFriend(int user1, int user2)
	{
		return
		client.sql("SELECT 1 FROM friends WHERE (user1=:user1 AND user2=:user2) OR (user1=:user2 AND user2=:user1)")
		.param("user1", user1)
		.param("user2", user2)
		.query()
		.listOfRows()
		.size()>0;
	}
	
	public List<Map<String, Object>> getFriendsForUser(int userId)
	{
		return
		client.sql("SELECT user1, user2 FROM friends WHERE user1=:userId OR user2=:userId")
		.param("userId", userId)
		.query()
		.listOfRows();
	}
	
	public void deleteFRequest(int user1, int user2)
	{
		client.sql("DELETE FROM friend_request WHERE (user_from=:user1 AND user_to=:user2) OR (user_from=:user2 AND user_to=:user1)")
		.param("user1", user1)
		.param("user2", user2)
		.update();
	}
	
	public void insertFriend(int user1, int user2)
	{
		client.sql("INSERT INTO friends(user1, user2) VALUES(:user1, :user2)")
		.param("user1", user1)
		.param("user2", user2)
		.update();
		
		deleteFRequest(user1, user2);
	}
	
	public void deleteFriend(int user1, int user2)
	{
		client.sql("DELETE FROM friends WHERE (user1=:user1 AND user2=:user2) OR (user1=:user2 AND user2=:user1)")
		.param("user1", user1)
		.param("user2", user2)
		.update();
	}
		
	public void updateLogin(int userId, String login)
	{
		client.sql("UPDATE users SET login=:login WHERE id=:userId")
		.param("login", login)
		.param("userId", userId)
		.update();
	}
	
	private static class ProfileRowMapper implements RowMapper<Profile>
	{	
				
		public Profile mapRow(ResultSet rs, int rowNum)  throws SQLException
		{
			int id = rs.getInt("user_id");
			String name = rs.getString("name");
			String surname = rs.getString("surname");
			LocalDate birthDate = rs.getObject("birth_date", LocalDate.class);
			MaritalStatus status = MaritalStatus.valueOf(rs.getString("marital_status"));
			String phone = rs.getString("phone");
			String country = rs.getString("country");
			String city = rs.getString("city");
			String about = rs.getString("about");
			String imagePath = rs.getString("image");
			
			return new Profile(id, name, surname, birthDate, status, phone, country, city, about, imagePath);
		}
		
	}
	
	// SETTINGS
	public void insertPrivacy(int userId, Privacy privacy)
	{
		client.sql("INSERT INTO user_privacy(user_id, birth_date, marital_status, phone, about) "
				+ "VALUES(:userId, :birthDate, :maritalStatus, :phone, :about)")
		.param("userId", userId)
		.param("birthDate", privacy.getBirthDate().toString())
		.param("maritalStatus", privacy.getMaritalStatus().toString())
		.param("phone", privacy.getPhone().toString())
		.param("about", privacy.getPhone().toString())
		.update();
	}
	
	public Privacy selectPrivacy(int userId)
	{
		return
		client.sql("SELECT * FROM user_privacy WHERE user_id=:userId")
		.param("userId", userId)
		.query(new PrivacyRowMapper())
		.single();		
	}
	
	public void updatePrivacy(int userId, Privacy privacy)
	{
		client.sql("UPDATE user_privacy SET birth_date=:birthDate, marital_status=:maritalStatus, "
				+ "phone=:phone, about=:about WHERE user_id=:userId")
		.param("userId", userId)
		.param("birthDate", privacy.getBirthDate().toString())
		.param("maritalStatus", privacy.getMaritalStatus().toString())
		.param("phone", privacy.getPhone().toString())
		.param("about", privacy.getAbout().toString())
		.update();
	}
	
	
	public List<Profile> selectBlockedUsers(int ownerId){
		
		String sql = "with acl_entries as (select acl_entry.sid, acl_entry.granting from acl_object_identity join acl_class \r\n"
				+ "on acl_object_identity.object_id_class = acl_class.id \r\n"
				+ "join acl_sid on acl_object_identity.owner_sid=acl_sid.id \r\n"
				+ "join acl_entry on acl_object_identity.id = acl_entry.acl_object_identity \r\n"
				+ "where acl_class.class = 'com.socialapp.main.Entities.Profile' -- класс объекта, для которого ищем permission\r\n"
				+ "AND acl_sid.sid=:ownerId -- владелец объекта, для которого ищем permission\r\n"
				+ "AND acl_entry.mask=:permission -- бит permission \r\n"
				+ ") \r\n"
				+ "select profiles.* from acl_entries join acl_sid on acl_entries.sid = acl_sid.id \r\n"
				+ "join users on users.id=acl_sid.sid::int \r\n"
				+ "join profiles on profiles.user_id=users.id \r\n"				
				+ "and acl_entries.granting = false";
		return
		client.sql(sql)
		.param("ownerId", String.valueOf(ownerId))
		.param("permission", MyPermission.VIEW_PROFILE.getMask())
		.query(new ProfileRowMapper())
		.list();		
	}
	
	private class PrivacyRowMapper implements RowMapper<Privacy>
	{
		@Override
		public Privacy mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			String birthDate = rs.getString("birth_date");
			String maritalStatus = rs.getString("marital_status");
			String phone = rs.getString("phone");
			String about = rs.getString("about");
			
			return new Privacy(PrivacyType.valueOf(birthDate), PrivacyType.valueOf(maritalStatus),
					PrivacyType.valueOf(phone), PrivacyType.valueOf(about));
		}
	}
	
	//========WallMessages========
	
	public int insertWallMessage(WallMessageIn message, String filename)
	{		
		logger.debug("insertWallMessage start");		
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		client.sql("INSERT INTO wall_messages(txt, image, sender, receiver, dt) VALUES(:txt, :image, :sender, :receiver, :dt)")
		.param("txt", message.getText())
		.param("image", filename)
		.param("sender", message.getSender())
		.param("receiver", message.getReceiver())		
		.param("dt", message.getDt())
		.update(keyHolder, "id");
		
		logger.debug("insertWallMessage success finish");
		
		return keyHolder.getKey().intValue();		
	}
	
	public List<WallMessageOut> selectWallMessages(int id)
	{
		return
		client.sql("select w.*, p.name || ' ' || p.surname as sender_name from wall_messages w join profiles p\r\n"
				+ "on w.sender = p.user_id \r\n"
				+ "where p.user_id=:id \r\n"
				+ "order by w.id desc")
		.param("id", id)		
		.query(new WallMessageOutRowMapper())
		.list();
	}
	
	public void deleteWallMessage(int id)
	{
		client.sql("DELETE FROM wall_messages WHERE id=:id")
		.param("id", id)
		.update();
	}
	
	// PHOTOS
	
	public void insertPhotos(String filename, int owner)
	{
		client.sql("INSERT INTO photos(user_id, path) VALUES(:user, :path)")		
		.param("user", owner)
		.param("path", filename)
		.update();
	}
	
	public List<Photo> getPhotos(int user)
	{
		return
		client.sql("SELECT * FROM photos WHERE user_id=:user ORDER BY id DESC")
		.param("user", user)
		.query(new PhotoRowMapper())
		.list();
	}
	
	/*
	 * @return имя удаленного файла
	 */
	public String deletePhoto(int userId, int id)
	{
		return
		client.sql("DELETE FROM photos WHERE id=:id AND user_id=:userId RETURNING path")
		.param("id", id)
		.param("userId", userId)
		.query(String.class)
		.single();
	}
	
	private static class WallMessageOutRowMapper implements RowMapper<WallMessageOut>
	{	
				
		public WallMessageOut mapRow(ResultSet rs, int rowNum)  throws SQLException
		{
			int id = rs.getInt("id");
			String text = rs.getString("txt");
			String imagePath = rs.getString("image");
			int sender = rs.getInt("sender");
			String senderName = rs.getString("sender_name");
			
			OffsetDateTime odt = rs.getObject("dt", OffsetDateTime.class);			
			var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");	
			String dt = odt.format(formatter);			
			
			var messageOut = new WallMessageOut();
			messageOut.setId(id);
			messageOut.setText(text);
			messageOut.setImagePath(imagePath);
			messageOut.setSenderId(sender);
			messageOut.setSenderName(senderName);
			messageOut.setDt(dt);	
			
			return messageOut;
		}
		
	}
	
	private class PhotoRowMapper implements RowMapper<Photo>
	{
		public Photo mapRow(ResultSet rs, int rowNum)  throws SQLException
		{
			int id = rs.getInt("id");
			int userId = rs.getInt("user_id");
			String path = rs.getString("path");
			
			var photo = new Photo();
			photo.setId(id);
			photo.setUserId(userId);
			photo.setPath(path);
			
			return photo;
		}
	}
	
	// CHANNELS
	
	/*
	 * Выводит список каналов, в которых пользователь владелец или подписчик
	 */
	public List<Channel> selectChannelsOfUser(int userId)
	{
		return // здесь CTE чтобы упорядочить группы, т.к. в UNION нельзя ORDER BY
		client.sql("WITH my_channels AS(\r\n"
				+ "SELECT * FROM channels WHERE owner=:userId ORDER BY id DESC\r\n"
				+ "),\r\n"
				+ "other_channels AS\r\n"
				+ "(\r\n"
				+ "SELECT ch.* FROM channels ch JOIN channel_subscribers chs ON ch.id=chs.channel\r\n"
				+ "WHERE ch.owner<>:userId AND chs.user_id=:userId ORDER BY ch.id DESC\r\n"
				+ ")\r\n"
				+ "SELECT * FROM my_channels\r\n"
				+ "UNION\r\n"
				+ "SELECT * FROM other_channels\r\n"						
				)
		.param("userId", userId)
		.query(new ChannelRowMapper())
		.list();
		
	}
	
	/*
	 * Выводит список каналов, в которых пользователь НЕ владелец и НЕ подписчик
	 */
	public List<Channel> selectChannelsOfNotUser(int userId)
	{
		return // здесь CTE чтобы упорядочить группы, т.к. в UNION нельзя ORDER BY
		client.sql( "SELECT ch.* FROM channels ch JOIN channel_subscribers chs ON ch.id=chs.channel \r\n"
				+ "WHERE ch.owner<>:userId AND chs.user_id<>:userId ORDER BY ch.id DESC\r\n"								
				)
		.param("userId", userId)
		.query(new ChannelRowMapper())
		.list();
		
	}
	
	public int insertChannel(Channel channel, int userId)
	{
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		client.sql("INSERT INTO channels(title, descr, owner) VALUES(:title, :descr, :owner)")
		.param("title", channel.getTitle())
		.param("descr", channel.getDescription())
		.param("owner", userId)
		.update(keyHolder, "id");
		
		return keyHolder.getKey().intValue();
	}
	
	public Channel selectChannel(int id)
	{
		return
		client.sql("SELECT * FROM channels WHERE id=:id")
		.param("id", id)
		.query(new ChannelRowMapper())
		.single();		
	}
	
	public void deleteChannel(int id)
	{
		client.sql("DELETE FROM channels WHERE id = :id")
		.param("id", id)
		.update();
	}
	
	public int insertChannelWallMessage(WallMessageIn message, String filename)
	{		
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		client.sql("INSERT INTO channel_wall_messages(txt, image, sender, channel, dt) VALUES(:txt, :image, :sender, :channel, :dt)")
		.param("txt", message.getText())
		.param("image", filename)
		.param("sender", message.getSender())
		.param("channel", message.getReceiver())		
		.param("dt", message.getDt())
		.update(keyHolder, "id");
		
		return keyHolder.getKey().intValue();		
	}
	
	public List<WallMessageOut> selectChannelWallMessages(int id)
	{
		return
		client.sql("select w.*, p.name || p.surname as sender_name from channel_wall_messages w join profiles p\r\n"
				+ "on w.sender = p.user_id\r\n"
				+ "order by w.id desc")
		.param("id", id)		
		.query(new WallMessageOutRowMapper())
		.list();
	}
	
	public void deleteChannelWallMessage(int id)
	{
		client.sql("DELETE FROM channel_wall_messages WHERE id=:id")
		.param("id", id)		
		.update();
	}
	
	public void deleteChannelWallMessage(int id, int userId)
	{
		client.sql("DELETE FROM channel_wall_messages WHERE id=:id AND sender=:userId")
		.param("id", id)
		.param("id", userId)
		.update();
	}
	
	public void insertSubscriber(int channelId, int userId)
	{
		client.sql("INSERT INTO channel_subscribers(channel, user_id) VALUES(:channelId, :userId)")
		.param("channelId", channelId)
		.param("userId", userId)
		.update();
	}
	
	public void deleteSubscriber(int channelId, int userId)
	{
		client.sql("DELETE FROM channel_subscribers WHERE channel=:channelId AND user_id=:userId")
		.param("channelId", channelId)
		.param("userId", userId)
		.update();
	}
	
	private class ChannelRowMapper implements RowMapper<Channel>
	{
		public Channel mapRow(ResultSet rs, int rowNum)  throws SQLException
		{
			int id = rs.getInt("id");
			String title = rs.getString("title");
			String description = rs.getString("descr");
			int ownerId = rs.getInt("owner");
			
			var channel = new Channel();
			channel.setId(id);
			channel.setTitle(title);
			channel.setDescription(description);
			
			Profile owner = selectProfile(ownerId);
			
			channel.setOwner(owner);
			
			return channel;
			
		}
	}
	
	//MyUserDetails
	
	public MyUserDetails getMyUserDetails(String username)
	{
		return
		client.sql("SELECT * FROM users WHERE login=:username")
		.param("username", username)
		.query(new MyUserDetailsRowMapper())
		.optional().orElseGet(()->null);
	}
	
	private class MyUserDetailsRowMapper implements RowMapper<MyUserDetails>
	{
		public MyUserDetails mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			int id = rs.getInt("id");
			String username = rs.getString("login");
			String password = rs.getString("password");
			boolean isEnabled = rs.getBoolean("enabled");
			
			String rolePrefix = grantedAuthorityDefaults.getRolePrefix();
			String role = rolePrefix + rs.getString("role").toUpperCase();
			
			SimpleGrantedAuthority granted = new SimpleGrantedAuthority(role);
			
			MyUserDetails details = new MyUserDetails(username, password, List.of(granted));
			details.setId(id);
			details.setEnabled(isEnabled);
			
			return details;			
		}		
	}
	
	//SessionRegistry
	
	public int insertUserSession(int userId, String sessionId, Date date, String ip)
	{
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		OffsetDateTime lastRequest = date.toInstant()
				  .atOffset(ZoneOffset.ofHours(3));
		
		client.sql("INSERT INTO user_sessions(user_id, session_id, last_request, ip)"
				+ " VALUES(:userId, :sessionId, :lastRequest, :ip)")
		.param("userId", userId)
		.param("sessionId", sessionId)		
		.param("lastRequest", lastRequest)
		.param("ip", ip)
		.update(keyHolder, "id");
		
		return keyHolder.getKey().intValue();		
	}
	
	public void deleteUserSession(String sessionId)
	{
		client.sql("DELETE FROM user_sessions WHERE session_id=:sessionId")
		.param("sessionId", sessionId)
		.update();
	}
	
	public void updateUserSession(String sessionId, Date lastRequest)
	{
		client.sql("UPDATE user_sessions SET last_request=:lastRequest WHERE session_id=:sessionId")
		.param("sessionId", sessionId)
		.param("lastRequest", lastRequest)
		.update();
	}
	
	public List<MySessionInformation> selectAllActiveMySessionInfo(int userId)	
	{
		String sql="SELECT * FROM user_sessions us JOIN users u on us.user_id=u.id"
				+ "  WHERE user_id=:userId AND last_request>=:date ORDER BY us.id DESC";			
		
		OffsetDateTime date = OffsetDateTime.now().minus(props.getTimeout());
		
		return
		client.sql(sql)
		.param("userId", userId)
		.param("date", date)
		.query(new MySessionInformationRowMapper())
		.list();
	}
	
	public List<SessionInformation> selectAllSessionInfo(int userId, boolean includeExpiredSessions)	
	{
		String sql="SELECT * FROM user_sessions us JOIN users u on us.user_id=u.id"
				+ "  WHERE user_id=:userId AND last_request>=:date ORDER BY us.id DESC";
		
		if(includeExpiredSessions)
			sql="SELECT * FROM user_sessions us JOIN users u on us.user_id=u.id"
					+ " WHERE user_id=:userId ORDER BY us.id DESC";
		
		OffsetDateTime date = OffsetDateTime.now().minus(props.getTimeout());
		
		return
		client.sql(sql)
		.param("userId", userId)
		.param("date", date)
		.query(new SessionInformationRowMapper())
		.list();
	}
	
	public MySessionInformation selectMySessionInfo(String sessionId)
	{
		return
		client.sql("SELECT us.*, u.login FROM user_sessions us JOIN users u ON us.user_id=u.id "
				+ "WHERE us.session_id=:sessionId")
		.param("sessionId", sessionId)		
		.query(new MySessionInformationRowMapper())
		.optional().orElseGet(()->null);	
	}
	
	public List<MyUserDetails> selectAllSessionPrincipals()
	{
		return
		client.sql("SELECT u.* FROM users u JOIN user_sessions us ON u.id = us.user_id")
		.query(new MyUserDetailsRowMapper())
		.list();
	}
	
	private class SessionInformationRowMapper implements RowMapper<SessionInformation>
	{
		public SessionInformation mapRow(ResultSet rs, int rowNum) throws SQLException
		{						
			String sessionId = rs.getString("session_id");
			OffsetDateTime dt = rs.getObject("last_request", OffsetDateTime.class);			
			String login = rs.getString("login");
			
			Date lastRequest = Date.from(dt.toInstant());
			
			MyUserDetails details = getMyUserDetails(login);
			
			return new SessionInformation(details, sessionId, lastRequest);
		}		
	}
	
	private class MySessionInformationRowMapper implements RowMapper<MySessionInformation>
	{
		public MySessionInformation mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			int id = rs.getInt("id");			
			String sessionId = rs.getString("session_id");
			OffsetDateTime dt = rs.getObject("last_request", OffsetDateTime.class);	
			String ip = rs.getString("ip");
			String login = rs.getString("login");
			
			Date lastRequest = Date.from(dt.toInstant());
			
			MyUserDetails details = getMyUserDetails(login);
			
			System.out.println("lastRequest:" + lastRequest);
			
			return new MySessionInformation(details, sessionId, lastRequest, id, ip);
		}		
	}
	
	//Для UserActivityFilter
	
	public void insertUserRequest(int userId, String request, OffsetDateTime dt)
	{
		client.sql("INSERT INTO user_activities(user_id, request, dt) VALUES(:userId, :request, :dt)")
		.param("userId", userId)
		.param("request", request)
		.param("dt", dt)
		.update();
	}
	
	// DatabaseRequestCache
	
	public void insertRequestCache(String sessionId, String uri)
	{	
		client.sql("INSERT INTO request_cache(session_id, url) VALUES(:sessionId, :uri)")
		.param("sessionId", sessionId)
		.param("uri", uri)
		.update();
	}
	
	public String selectRequestCache(String sessionId)
	{
		return
		(String)
		client.sql("SELECT url FROM request_cache WHERE session_id=:sessionId ORDER BY id DESC LIMIT 1")
		.param("sessionId",sessionId)
		.query()
		.optionalValue()
		.orElseGet(()->null);
	}
	
	public void updateRequestCache(String sessionId, String oldSessionId)
	{
		client.sql("UPDATE request_cache SET session_id=:sessionId WHERE session_id=:oldSessionId")
		.param("sessionId", sessionId)
		.param("oldSessionId", oldSessionId)
		.update();
	}
	
	public void deleteRequestCache(String sessionId) 
	{		
		client.sql("DELETE FROM request_cache WHERE session_id=:sessionId")
		.param("sessionId", sessionId)		
		.update();
	}
	
	//Auth event log
	
	public void insertAuthLogEntry(String login, String description, OffsetDateTime dt ,boolean isSuccess)
	{
		client.sql("INSERT INTO auth_logs(login, description, ts, is_success) VALUES(:login, :description, :ts, :isSuccess)")
		.param("login", login)
		.param("description", description)
		.param("ts", dt)
		.param("isSuccess", isSuccess)
		.update();
	}
	
	public void insertAuthzLogEntry(String login, String description, OffsetDateTime dt ,boolean isSuccess)
	{
		client.sql("INSERT INTO authz_logs(login, description, ts, is_success) VALUES(:login, :description, :ts, :isSuccess)")
		.param("login", login)
		.param("description", description)
		.param("ts", dt)
		.param("isSuccess", isSuccess)
		.update();
	}
}
