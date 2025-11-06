package mainPackage.Proxying;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import mainPackage.MainLogger;

/*
 * Проксирует некоторые методы интерфейса mainPackage.Interfaces.IRepository
 * Методы в этом классе называются аналогично проксируемым методам интерфейса, для которых они определены
 */
@Aspect
@Component
public class RepositoryAspect {
	
	private final int maxRepeats = 5;
	private int curRepeat = 0;
	
	@Pointcut("execution(* mainPackage.Interfaces.IRepository.insertRiaNews(*))")
	public void insertRiaNews() {}
	
	@Pointcut("execution(* mainPackage.Interfaces.IRepository.insertStarhitNews(*))")
	public void insertStarhitNews() {}
	
	@Pointcut("execution(* mainPackage.Interfaces.IRepository.insertVcArticle(*))")
	public void insertVcArticle() {}
	
	@Pointcut("execution(* mainPackage.Interfaces.IRepository.insertHoroscope(*))")
	public void insertHoroscope() {}	
	
	@Pointcut("execution(* mainPackage.Interfaces.IRepository.insertVideo(*))")
	public void insertVideo() {}
	
	@Pointcut("execution(* mainPackage.Interfaces.IRepository.insertWiki(*))")
	public void insertWiki() {}
	
	
	@Around("insertRiaNews()||insertStarhitNews()||insertVcArticle()||insertHoroscope()||insertVideo()||insertWiki()")	
	Object handler(ProceedingJoinPoint point)
	{
		MainLogger.logger.fine("RepositoryAspect handler() start in " + Thread.currentThread().getName());		
		do
		{			
			try
			{
				Object result = point.proceed();
				MainLogger.logger.fine("RepositoryAspect handler() success. Repeat: " 
						+ curRepeat + ". " + Thread.currentThread().getName());
				return result;
			}
			catch(Throwable ex)
			{
				MainLogger.logger.warning("****** RepositoryAspect handler() exception. Repeat: " 
						+ curRepeat + ". Class: " + point.getTarget().getClass() + ". "
						+ Thread.currentThread().getName());
				MainLogger.logger.warning("****** " + ex.getCause().getMessage());
				curRepeat++;
			}			
		}
		while(curRepeat<maxRepeats);
		
		MainLogger.logger.fine("-----RepositoryAspect handler() finish in" + Thread.currentThread().getName());
		
		return null;
	}

}
