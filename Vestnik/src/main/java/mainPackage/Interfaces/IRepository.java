package mainPackage.Interfaces;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import mainPackage.Entities.Horoscope;
import mainPackage.Entities.News;
import mainPackage.Entities.SearchResult;
import mainPackage.Entities.VcArticle;
import mainPackage.Entities.Video;
import mainPackage.Enums.HoroscopeSign;

/*
 * Реализации методов в классах в случае исключений должны обязательно выбрасывать исключения наружу,
 * чтобы аспект перехватывал их и запускал выполнение определенного метода заново.
 */
public interface IRepository {
	
	int insertRiaNews(News news) throws Exception;
	
	List<News> getLastRiaNews() throws Exception;
	
	News getRiaNews(int id) throws Exception;
	
	boolean updateRiaNews(News news) throws Exception;
	
	public boolean deleteRiaNews(int id) throws Exception;
	
	
	int insertStarhitNews(News news) throws Exception;
	
	List<News> getLastStarhitNews() throws Exception;
	
	News getStarhitNews(int id) throws Exception;
	
	
	int insertVcArticle(VcArticle article) throws Exception;
	
	List<VcArticle> getLastVcArticles() throws Exception;
	
	VcArticle getVcArticle(int id) throws Exception;
	
	
	int insertHoroscope(Horoscope horo) throws Exception;
	
	Horoscope getHoroscope(HoroscopeSign sign) throws Exception;
	
	
	int insertVideo(Video video) throws Exception;
	
	List<Video> getLastVideos() throws Exception;
	
	Video getVideo(int id) throws Exception;
	
	
	void insertWiki(String str) throws Exception;
	
	String getLastDayEvents() throws Exception;
	
	
	void insertLastUpdate() throws Exception;
	
	ZonedDateTime getLastUpdate() throws Exception;
	
	
	SearchResult search(String query, int pageNum, int pageSize) throws Exception;

}
