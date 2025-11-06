package mainPackage.Interfaces;

import java.util.concurrent.Callable;

/* Методы этого интерфейса проксируются в mainPackage.Proxying.ParserInvocationHandler
 * 
 * Методы, переопределяющие метод call() из интерфейса Callable, при возникновении исключений должны выбрасывать их
 * наружу, чтобы прокси мог их перехватить и запустить выполнение метода заново.
 *  
 */
public interface IParser extends Callable<Void> {

}
