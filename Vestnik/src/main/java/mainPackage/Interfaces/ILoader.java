package mainPackage.Interfaces;

import java.util.Queue;

/* 
 * Методы этого интерфейса проксируются аспектом mainPackage.Proxying.LoaderAspect
 */
public interface ILoader {
	
	/*
	 * Реализация этого метода в классах в случае исключений должна обязательно выбрасывать исключение из метода,
	 * чтобы аспект перехватывал их и запускал выполнение метода заново.
	 */
	Queue<String> load() throws Exception;

}
