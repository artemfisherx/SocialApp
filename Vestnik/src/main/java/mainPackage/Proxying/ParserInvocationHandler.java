package mainPackage.Proxying;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import mainPackage.MainLogger;
import mainPackage.Interfaces.IParser;

/*
 * Проксирует некоторые методы интерфейса mainPackage.Interfaces.IParser
 * Методы в этом классе называются аналогично проксируемым методам интерфейса, для которых они определены
 */
public class ParserInvocationHandler implements InvocationHandler{
		
	private final int maxRepeats = 5;
	private int curRepeat = 0;
	private IParser parser;
	
	public ParserInvocationHandler(IParser parser)
	{
		this.parser = parser;		
	}
	
	@Override
	public Object invoke(Object proxy, Method m, Object [] args)
	{
		MainLogger.logger.fine("ParserInvocationHandler start in " + Thread.currentThread().getName());
		do
		{			
			try
			{
				Object result = m.invoke(parser, args);
				MainLogger.logger.fine("ParserInvocationHandler success. Repeat: " + curRepeat 
						+ ". " + Thread.currentThread().getName());
				return result;
			}
			catch(Exception ex)
			{
				MainLogger.logger.warning("ParserInvocationHandler exception. Repeat: " + curRepeat
						+ ". " + Thread.currentThread().getName());
				MainLogger.logger.warning(ex.getCause().getMessage());
				curRepeat++;
			}			
		}
		while(curRepeat<maxRepeats);
		
		MainLogger.logger.fine("ParserInvocationHandler finish in" + Thread.currentThread().getName());
		
		return null;
		
	}

}
