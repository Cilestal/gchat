package dp.ua.michaellang.gchat.db;

import dp.ua.michaellang.gchat.db.model.User;

import java.util.List;

/**
 * Date: 14.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public interface UserDB {
    List<User> getUserList();
    void addUser(User user) throws Exception;
    boolean removeUser(User user);
    User getUser(String login);
    User getUserById(int id);
}
