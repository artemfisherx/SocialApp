package mainPackage.Interfaces;

import java.util.concurrent.Callable;

/*
 * Используется для того, чтобы перехватывать исключения в отдельном аспекте
 */

public interface IStarter extends Callable<Void>{

}
