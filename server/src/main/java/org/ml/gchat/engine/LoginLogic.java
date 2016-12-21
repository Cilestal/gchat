package org.ml.gchat.engine;

/**
 * Date: 13.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public interface LoginLogic {
    boolean checkLogin(Message mes);
    void registerLogin(Message mes) throws Exception;
}
