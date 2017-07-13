package dp.ua.michaellang.gchat.db;

import dp.ua.michaellang.gchat.db.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 14.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class TestUserBD implements UserDB {
    private List<User> list;

    public TestUserBD() {
        list = new ArrayList<>();
    }

    /**
     * Создает двух случайных пользователей в базе данных.
     *
     * @throws Exception При вводе некорректного пароля
     */
    public void createUsers() throws Exception {
        User user1 = new User("Alex");
        user1.createPassword("are84159_w23241Azs", false);

        User user2 = new User("Michael");
        user2.createPassword("1111", false);

        list.add(user1);
        list.add(user2);
    }

    @Override
    public List<User> getUserList() {
        return list;
    }

    @Override
    public void addUser(User user) {
        list.add(user);
    }

    @Override
    public boolean removeUser(User user) {
        return list.remove(user);
    }

    @Override
    public User getUser(String login){
        for (User user : list) {
            if(user.getLogin().equals(login)) return user;
        }
        return null;
    }

    @Override
    public User getUserById(int id) {
        for (User user : list) {
            if(user.getId() == id) return user;
        }
        return null;
    }
}
