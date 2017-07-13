package dp.ua.michaellang.gchat.db;

import dp.ua.michaellang.gchat.db.model.User;
import dp.ua.michaellang.gchat.db.model.UserList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Date: 14.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class XMLUserBD implements UserDB {
    private final String file;
    private final String schemaFile;

    private UserList userList;

    /**
     * Хранит значение, используемое для автоинкремента
     */
    private int incId;

    public XMLUserBD(String file) {
        this(file, null);
    }

    public XMLUserBD(String file, String schema) {
        this.file = file;
        this.schemaFile = schema;
        this.userList = new UserList();
        this.incId = 0;
    }

    /**
     * Загружает базу данных xml из файла {@see file} в список {@link UserList#users}
     *
     * @throws JAXBException
     * @throws SAXException
     * @throws IOException
     */
    public void loadFromXML() throws JAXBException, SAXException, IOException {
        JAXBContext jc = JAXBContext.newInstance(UserList.class);
        Unmarshaller unm = jc.createUnmarshaller();

        if(schemaFile != null) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(this.schemaFile));
            unm.setSchema(schema);
        }

        try(FileReader reader = new FileReader(this.file)){
            //unmarshalling
            this.userList = (UserList) unm.unmarshal(reader);
        }

        List<User> userList = getUserList();
        User lastUser = userList.get(userList.size() - 1);
        this.incId = lastUser.getId() + 1;
    }

    /**
     * Сохраняет список пользователей {@link UserList#users} в файл {@see file}
     *
     * @throws JAXBException
     * @throws SAXException
     * @throws IOException
     */
    public void storeToXML() throws JAXBException, SAXException, IOException {
        JAXBContext jc = JAXBContext.newInstance(UserList.class);
        Marshaller mar = jc.createMarshaller();

        if(schemaFile != null) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(schemaFile));

            //We will leverage JAXBSource to expose our JAXB object model as an XML input to the Validator.
            Source source = new JAXBSource(mar, this.userList);

            //This Validator can accept different types of XML inputs.
            Validator validator = schema.newValidator();
            validator.validate(source);
        }

        try(FileWriter fw = new FileWriter(file)){
            //marshalling
            mar.marshal(this.userList, fw);
        }
    }

    @Override
    public List<User> getUserList() {
        return userList.getUserList();
    }

    @Override
    public void addUser(User user) throws Exception {
        if(user.getPasswordHash() == null) throw new IncorrectPasswordException();

        String login = user.getLogin();

        if(getUser(login) != null){
            throw new UserAlreadyExistsException();
        } else {
            user.setId(incId);
            incId++;
            userList.getUserList().add(user);
        }
    }

    @Override
    public boolean removeUser(User user) {
        return userList.getUserList().remove(user);
    }

    @Override
    public User getUser(String login) {
        for (User user : userList.getUserList()) {
            if (user.getLogin().equals(login)) return user;
        }
        return null;
    }

    @Override
    public User getUserById(int id) {
        for (User user : userList.getUserList()) {
            if (user.getId() == id) return user;
        }
        return null;
    }

    public int getIncId() {
        return incId;
    }
}
