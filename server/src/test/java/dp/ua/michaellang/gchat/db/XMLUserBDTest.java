package dp.ua.michaellang.gchat.db;

import dp.ua.michaellang.gchat.db.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Date: 15.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
@RunWith(Parameterized.class)
public class XMLUserBDTest {
    XMLUserBD bd;
    String file;
    String schema;

    public XMLUserBDTest(String file, String schema) {
        this.file = file;
        this.schema = schema;
    }

    @Before
    public void setUp() {
        bd = new XMLUserBD(file, schema);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data(){
        Object[][] objects = {
                {"src/test/resources/user_list.xml",
                        "src/test/resources/user_list.xsd"},
                {"src/test/resources/user_list.xml", null}
        };
        return Arrays.asList(objects);
    }

    @Test
    public void testLoadFromXML() throws IOException, JAXBException, SAXException {
        bd.loadFromXML();
        Assert.assertTrue(bd.getUserList().size() > 0);
    }

    @Test
    public void testGetUserList() {
        Assert.assertNotNull(bd.getUserList());
    }

    @Test
    public void testAddUser() throws Exception {
        System.out.println(bd.getUserList());
        bd.addUser(new User("Michael", "passwordHash", "salt"));
        Assert.assertTrue(bd.getUserList().size() != 0);
        Assert.assertNotNull(bd.getUser("Michael"));
        System.out.println(bd.getUserList());
    }

    @Test
    public void testRemoveUser() throws Exception {
        User user = new User("Michael", "passwordHash", "salt");
        bd.addUser(user);
        bd.removeUser(user);

        Assert.assertNull(bd.getUser("Michael"));
    }

    @Test
    public void testGetUser() throws Exception {
        User user = new User("Michael", "passwordHash", "salt");
        bd.addUser(user);

        Assert.assertNotNull(bd.getUser("Michael"));
    }

    @Test
    public void testGetUserById() throws Exception {
        for (int i = 0; i < 10; i++) {
            User user = new User("Michael" + i, "passwordHash", "salt");
            bd.addUser(user);
            Assert.assertNotNull(bd.getUserById(i));
        }
    }

    @Test
    public void testStoreToXML() throws Exception {
        User user1 = new User("Alex");
        user1.createPassword("are84159_w23241Azs", false);

        User user2 = new User("Michael");
        user2.createPassword("1111", false);

        bd.addUser(user1);
        bd.addUser(user2);

        bd.storeToXML();

        bd.loadFromXML();
        Assert.assertTrue(bd.getUserList().size() == 2);
        Assert.assertNotNull(bd.getUser("Alex"));
        Assert.assertNotNull(bd.getUser("Michael"));
    }
}