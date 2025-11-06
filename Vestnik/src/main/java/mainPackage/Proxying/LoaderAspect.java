package mainPackage.Proxying;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import mainPackage.MainLogger;

/*
 * Проксирует некоторые методы интерфейса mainPackage.Interfaces.ILoader
 * Методы в этом классе называются аналогично проксируемым методам интерфейса, для которых они определены
 */

@Aspect
@Component
public class LoaderAspect {
	
	private final int maxRepeats = 5;
	private int curRepeat = 0;	
	
	@Around("execution(* mainPackage.Interfaces.ILoader.load())")
	Object load(ProceedingJoinPoint point)
	{
		MainLogger.logger.fine("LoaderAspect load() start in " + Thread.currentThread().getName()) ;
		do
		{
			try
			{
				Object result = point.proceed();				
				MainLogger.logger.fine("LoaderAspect load() success. Repeat: " + curRepeat 
						+ ". " + Thread.currentThread().getName());
				return result;
			}
			catch(Throwable ex)
			{
				MainLogger.logger.warning("LoaderAspect load() exception. Repeat: " + curRepeat 
						+ ". Class: " + point.getTarget().getClass() + ". " + Thread.currentThread().getName());
				MainLogger.logger.warning(ex.getCause().getMessage());
				curRepeat++;
			}
		}
		while(curRepeat<maxRepeats);
		
		MainLogger.logger.fine("LoaderAspect load() finish in " + Thread.currentThread().getName());
		
		return null;
	}
	

}
