package dp.ua.michaellang.gchat.engine;

import dp.ua.michaellang.gchat.db.model.User;
import dp.ua.michaellang.gchat.db.TestUserBD;
import dp.ua.michaellang.gchat.db.UserDB;
import dp.ua.michaellang.gchat.db.XMLUserBD;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Date: 14.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
@RunWith(Parameterized.class)
public class CommonLoginLogicTest {
    UserDB testBD;
    CommonLoginLogic logic;

    public CommonLoginLogicTest(UserDB testBD) {
        this.testBD = testBD;
        this.logic = new CommonLoginLogic(testBD);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] objects = {
                {new TestUserBD()},
                {new XMLUserBD("src\\test\\resources\\user_list.xml", "src\\test\\resources\\user_list.xsd")}
        };
        return Arrays.asList(objects);
    }

    @Test
    public void testCheckLogin() throws Exception {
        List<User> userList = testBD.getUserList();
        User user1 = new User("Alex");
        user1.createPassword("are84159_w23241Azs", false);

        User user2 = new User("Michael");
        user2.createPassword("1111", false);

        userList.add(user1);
        userList.add(user2);

        Message authKey = new Message(Command.USER_AUTH, "Michael", new Date(), "1111");
        Message wrongAuthKey = new Message(Command.USER_AUTH, "Alex", new Date(), "wrongAuthKey");

        Assert.assertFalse(logic.checkLogin(wrongAuthKey));
        Assert.assertTrue(logic.checkLogin(authKey));
    }
}