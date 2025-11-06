package mainPackage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.SmartLifecycle;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import mainPackage.Entities.Horoscope;
import mainPackage.Entities.News;
import mainPackage.Entities.SearchResult;
import mainPackage.Entities.SearchResultItem;
import mainPackage.Entities.VcArticle;
import mainPackage.Entities.Video;
import mainPackage.Enums.HoroscopeSign;
import mainPackage.Interfaces.IRepository;

@Repository("mainRepository")
@Transactional
public class MainRepository implements IRepository, SmartLifecycle{
	
	private JdbcTemplate jdbc;
	
	public MainRepository(DataSource source)
	{
		this.jdbc = new JdbcTemplate(source);
	}
	
	/************ Life cycle ****************/
	private volatile boolean isRunning = false;
	
	@Override
	public void start()
	{
		MainLogger.logger.fine("+++ MainRepository Lifecycle start");
		this.isRunning = true;
	}
	
	@Override
	public int getPhase()
	{
		return 0;
	}
	
	@Override
	public boolean isAutoStartup()
	{
		return true;
	}
	
	@Override
	public boolean isRunning()
	{
		return this.isRunning;
	}
	
	@Override
	public void stop(Runnable callback)
	{
		MainLogger.logger.fine("+++ MainRepository Lifecycle stop runnable");
		callback.run();
		this.isRunning = false;
	}
	
	@Override
	public void stop()
	{
		MainLogger.logger.fine("+++ MainRepository Lifecycle stop");	
		this.isRunning = false;
	}
	
	
	
	
	/************* Ria news methods *********/
	
	@Override
	public int insertRiaNews(News news) throws Exception
	{
		MainLogger.logger.fine("MainRepository insertNews start");		
		
		String sql = "INSERT INTO ria_news(title, txt, dt, img, zone_offset, link) VALUES(?,?,?,?,?,?);";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.update(con->{
			
			MainLogger.logger.fine("MainRepository insertRiaNews jdbc.update start");
			
			PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"});
			
			Timestamp ts = Timestamp.from(news.getDt().toInstant());
			
			ps.setString(1, news.getTitle());
			ps.setString(2, news.getText());
			ps.setObject(3, ts);		
			ps.setString(4, news.getImg());
			ps.setString(5, news.getDt().getOffset().toString());
			ps.setString(6, news.getLink());
			
			MainLogger.logger.fine("MainRepository insertRiaNews jdbc.update finish");
			
			return ps;
			
		}, keyHolder);
		
		MainLogger.logger.fine("MainRepository insertRiaNews finish");
		
		return keyHolder.getKey().intValue();
	}
	
	@Override
	public List<News> getLastRiaNews() throws Exception
	{
		String sql="SELECT id, title, txt, dt, img, zone_offset, link FROM ria_news ORDER BY dt DESC LIMIT 6";
		return jdbc.query(sql, new NewsRowMapper());
	}
	
	@Override
	@Cacheable("riaNewsCache")
	public News getRiaNews(int id) throws Exception
	{		
		String sql = "SELECT id, title, txt, dt, img, zone_offset, link FROM ria_news WHERE id=?";
		var news = jdbc.queryForObject(sql, new NewsRowMapper(), id);
		return news;
	}
	
	@Transactional(isolation = Isolation.SERIALIZABLE)
	@Override	
	public boolean updateRiaNews(News news) throws Exception
	{
		String sql = "UPDATE ria_news SET "
				+ "title = ?, txt=?, dt=?, img=?, zone_offset=?, link=?"			
				+ " WHERE id=?;";
		
		jdbc.update(sql, news.getTitle(), news.getText(), news.getDt(), 
				news.getImg(),news.getDt().getOffset().toString(), news.getLink(), news.getId());
		return true;
	}
	
	public boolean deleteRiaNews(int id) throws Exception
	{
		String sql = "DELETE FROM ria_news WHERE id=?";
		jdbc.update(sql, id);
		return true;
	}
	
	class NewsRowMapper implements RowMapper<News>{
		
		public News mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			int id = rs.getInt("id");
			String title = rs.getString("title");
			String txt = rs.getString("txt");
			OffsetDateTime dt = rs.getObject("dt", OffsetDateTime.class);			
			String img = rs.getString("img");			
			ZoneOffset offset = ZoneOffset.of(rs.getString("zone_offset"));
			String link = rs.getString("link");
						
			dt = dt.withOffsetSameInstant(offset);
			
			return new News(id, title, txt, dt, img, link);
		}
		
	}
	
/************* Starhit news methods *********/
	
	@Override
	public int insertStarhitNews(News news) throws Exception
	{
		MainLogger.logger.fine("MainRepository insertNews start");		
		
		String sql = "INSERT INTO starhit_news(title, txt, dt, img, zone_offset, link) VALUES(?,?,?,?,?,?);";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.update(con->{
			
			MainLogger.logger.fine("MainRepository insertStarhitNews jdbc.update start");
			
			PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"});
			
			Timestamp ts = Timestamp.from(news.getDt().toInstant());
			
			ps.setString(1, news.getTitle());
			ps.setString(2, news.getText());
			ps.setObject(3, ts);		
			ps.setString(4, news.getImg());
			ps.setString(5, news.getDt().getOffset().toString());
			ps.setString(6, news.getLink());
			
			MainLogger.logger.fine("MainRepository insertStarhitNews jdbc.update finish");
			
			return ps;
			
		}, keyHolder);
		
		MainLogger.logger.fine("MainRepository insertStarhitNews finish");
		
		return keyHolder.getKey().intValue();
	}
	
	@Override
	public List<News> getLastStarhitNews() throws Exception
	{
		String sql="SELECT id, title, txt, dt, img, zone_offset, link FROM starhit_news ORDER BY dt DESC LIMIT 6";
		return jdbc.query(sql, new NewsRowMapper());
	}
	
	@Override
	@Cacheable("starhitCache")
	public News getStarhitNews(int id) throws Exception
	{
		String sql = "SELECT id, title, txt, dt, img, zone_offset, link FROM starhit_news WHERE id=?";
		var news = jdbc.queryForObject(sql, new NewsRowMapper(), id);
		return news;
	}
		
	
/************* Horoscope articles methods *********/
	
	@Override
	public int insertHoroscope(Horoscope horo) throws Exception
	{
		MainLogger.logger.fine("MainRepository insertHoroscope start");		
		
		String sql = "INSERT INTO horoscopes(sign, dt, txt) VALUES(?,?,?);";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.update(con->{
			
			MainLogger.logger.fine("MainRepository insertHoroscope jdbc.update start");
			
			PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"});	
			
			ps.setString(1, horo.getSign().toString());			
			ps.setObject(2, horo.getDate().toString(), Types.DATE);			
			ps.setString(3, horo.getText());			
			
			MainLogger.logger.fine("MainRepository insertHoroscope jdbc.update finish");
			
			return ps;
			
		}, keyHolder);
		
		MainLogger.logger.fine("MainRepository insertHoroscope finish");
		
		return keyHolder.getKey().intValue();
	}
		
	
	@Override
	public Horoscope getHoroscope(HoroscopeSign sign) throws Exception
	{
		String sql = "SELECT id, sign, txt, dt FROM horoscopes WHERE sign=? ORDER BY dt DESC LIMIT 1";
		var horo = jdbc.query(sql, new HoroscopeExtractor(), sign.toString());
		return horo;
	}
	
	class HoroscopeExtractor implements ResultSetExtractor<Horoscope>{
		
		public Horoscope extractData(ResultSet rs) throws SQLException
		{
			if(!rs.next())
				return null;
				
			int id = rs.getInt("id");
			HoroscopeSign sign =  HoroscopeSign.valueOf(rs.getString("sign"));
			String txt = rs.getString("txt");
			LocalDate dt = LocalDate.parse(rs.getString("dt"));		
			
			return new Horoscope(id, sign, dt, txt);
		}
		
	}

	
/************* Vc articles methods *********/
	
	@Override
	public int insertVcArticle(VcArticle article) throws Exception
	{
		MainLogger.logger.fine("MainRepository insertVcArticle start");		
		
		String sql = "INSERT INTO vc_articles(title, txt, dt, zone_offset, link) VALUES(?,?,?,?,?);";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.update(con->{
			
			MainLogger.logger.fine("MainRepository insertVcArticle jdbc.update start");
			
			PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"});			
			Timestamp ts = Timestamp.from(article.getDt().toInstant());
			
			ps.setString(1, article.getTitle());
			ps.setString(2, article.getText());
			ps.setObject(3, ts);
			ps.setString(4, article.getDt().getOffset().toString());
			ps.setString(5, article.getLink());
			
			MainLogger.logger.fine("MainRepository insertVcArticle jdbc.update finish");
			
			return ps;
			
		}, keyHolder);
		
		MainLogger.logger.fine("MainRepository insertVcArticle finish");
		
		return keyHolder.getKey().intValue();
	}
	
	@Override
	public List<VcArticle> getLastVcArticles() throws Exception
	{
		String sql="SELECT id, title, txt, dt, zone_offset, link FROM vc_articles ORDER BY dt DESC LIMIT 6";
		return jdbc.query(sql, new VcArticleRowMapper());
	}
	
	@Override
	@Cacheable("vcCache")
	public VcArticle getVcArticle(int id) throws Exception
	{
		String sql = "SELECT id, title, txt, dt, zone_offset, link FROM vc_articles WHERE id=?";
		var vcArticle = jdbc.queryForObject(sql, new VcArticleRowMapper(), id);
		return vcArticle;
	}
	
	class VcArticleRowMapper implements RowMapper<VcArticle>{
		
		public VcArticle mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			int id = rs.getInt("id");
			String title = rs.getString("title");
			String txt = rs.getString("txt");
			OffsetDateTime dt = rs.getObject("dt", OffsetDateTime.class);	
			ZoneOffset offset = ZoneOffset.of(rs.getString("zone_offset"));
			String link = rs.getString("link");			
			
			dt = dt.withOffsetSameInstant(offset);		
			
			return new VcArticle(id, title, txt, dt, link);
		}
		
	}
	
	
/************* Rutube videos methods *********/
	
	@Override
	public int insertVideo(Video video) throws Exception
	{
		MainLogger.logger.fine("MainRepository insertVideo start");		
		
		String sql = "INSERT INTO videos(name, duration, img, link, dt) VALUES(?,?,?,?,?);";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.update(con->{
			
			MainLogger.logger.fine("MainRepository insertVideo jdbc.update start");
			
			PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"});			
			Timestamp ts = Timestamp.valueOf(LocalDateTime.now());		
			
			int duration = video.getDuration().getHour()*60*60 + video.getDuration().getMinute()*60
					+video.getDuration().getSecond();
									
			ps.setString(1, video.getName());
			ps.setObject(2, duration + " " + "second", Types.OTHER);			
			ps.setString(3, video.getImg());
			ps.setString(4, video.getLink());
			ps.setObject(5, ts);
			
			MainLogger.logger.fine("MainRepository insertVideo jdbc.update finish");
			
			return ps;
			
		}, keyHolder);
		
		MainLogger.logger.fine("MainRepository insertVideo finish");
		
		return keyHolder.getKey().intValue();
	}
	
	@Override
	public List<Video> getLastVideos() throws Exception
	{
		String sql="SELECT id, name, duration, img, link, dt FROM videos ORDER BY id DESC LIMIT 6";
		return jdbc.query(sql, new VideoRowMapper());
	}
	
	@Override
	@Cacheable("rutubeCache")
	public Video getVideo(int id) throws Exception
	{
		String sql = "SELECT id, name, duration, img, link, dt FROM videos WHERE id=?";
		var video = jdbc.queryForObject(sql, new VideoRowMapper(), id);
		return video;
	}
	
	class VideoRowMapper implements RowMapper<Video>{
		
		public Video mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			int id = rs.getInt("id");
			String name = rs.getString("name");			
			String interval = rs.getString("duration");	
			
			Pattern pattern = Pattern.compile("(?<hour>\\d{2}):(?<min>\\d{2}):(?<sec>\\d{2})");
			Matcher matcher = pattern.matcher(interval);
			
			LocalTime duration = LocalTime.MIN;
			if(matcher.find())
			{	
				duration = duration.plusHours(Long.parseLong(matcher.group("hour")));
				duration = duration.plusMinutes(Long.parseLong(matcher.group("min")));
				duration = duration.plusSeconds(Long.parseLong(matcher.group("sec")));
			}
			
			String img = rs.getString("img");
			String link = rs.getString("link");		
						
			return new Video(id, name, duration, img, link);
		}
		
	}
	
/****** Wikipedia *****************/
	
	@Override
	public void insertWiki(String str) throws Exception
	{
		MainLogger.logger.fine("MainRepository insertWiki start");	
		
		String sql = "INSERT INTO day_events(txt) VALUES(?)";
		
		jdbc.update(sql, str);
		
		MainLogger.logger.fine("MainRepository insertWiki finish");
	}
	
	@Override
	public String getLastDayEvents() throws Exception
	{
		String sql = "SELECT dt, txt FROM day_events ORDER BY dt DESC LIMIT 1";
		String result="";
		try
		{			
			result = ((String)jdbc.queryForMap(sql).getOrDefault("txt", "")).trim();
		}
		catch(IncorrectResultSizeDataAccessException ex)
		{
			MainLogger.logger.warning("MainRepository getLastDayEvents() exception: " + ex.getMessage());
		}		
		
		return result;
		
	}
	
/******* Last updates *********************/
	
	@Override
	public void insertLastUpdate() throws Exception
	{
		String sql = "INSERT INTO last_updates(dt) VALUES(default)";
		jdbc.update(sql);
	}
	
	@Override
	public ZonedDateTime getLastUpdate() throws Exception
	{		
		try
		{
			String sql = "SELECT dt FROM last_updates ORDER by dt DESC LIMIT 1";
			return jdbc.queryForObject(sql, new RowMapper<ZonedDateTime>()
					{
						@Override
						public ZonedDateTime mapRow(ResultSet rs, int rowNum) throws SQLException
						{
							return rs.getObject("dt", OffsetDateTime.class)
									.atZoneSameInstant(ZoneId.systemDefault());
						}
					});
		}
		catch(IncorrectResultSizeDataAccessException ex)
		{
			MainLogger.logger.warning("MainRepository getLastUpdate() exception: " + ex.getMessage());
		}
		
		return null;
		
	}
	
	/************** SEARCH **********************/
	
	@Override
	public SearchResult search(String query, int pageNum, int pageSize) throws Exception
	{
		String sql = "SELECT * FROM search_text(?) as (id int, title text, txt text, orig_table text)";
		
		return jdbc.query(sql, new SearchResultSetExtractor(pageNum, pageSize),query);
				
	}
	
	
	class SearchResultSetExtractor implements ResultSetExtractor<SearchResult>
	{
		
		private int pageNum;
		private int pageSize;
		
		SearchResultSetExtractor(int pageNum, int pageSize)
		{
			this.pageNum = pageNum;
			this.pageSize = pageSize;
		}
		
		
		@Override
		public SearchResult extractData(ResultSet rs) throws SQLException
		{
			int start = (pageNum-1)*pageSize+1;
			int finish = pageNum*pageSize;
						
			
			int count = rs.getFetchSize();
			Collection<SearchResultItem> items = new ArrayList<>();
			
			CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
			crs.populate(rs);
			
			int i=1;
			while(crs.next()&&i<=finish)
			{
				if(i<start) continue;
				
				int id = crs.getInt("id");
				String title = crs.getString("title");
				String text = crs.getString("txt");
				String table = crs.getString("orig_table");
				items.add(new SearchResultItem(i, id, title, text, table));
				
				MainLogger.logger.fine("SearchResultSetExtractor extractData id:"+id);
				
				i++;
			}
			
			return new SearchResult(count, items);
			
		}
		
	}

}
