
package dp.ua.michaellang.gchat.db.model;

import dp.ua.michaellang.gchat.util.CryptoUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.security.SecureRandom;

/**
 * <p>Java class for userType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="userType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="login" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="passwordHash" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="salt" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "user", propOrder = {
    "id",
    "login",
        "passwordHash",
    "salt"
})
public class User {

    protected int id;
    @XmlElement(required = true)
    protected String login;
    @XmlElement(name = "password", required = true)
    protected String passwordHash;
    @XmlElement(required = true)
    protected String salt;


    public User() {
    }

    public User(String login) {
        this.login = login;
    }

    public User(int id, String login) {
        this.id = id;
        this.login = login;
    }

    public User(int id, String login, String passwordHash, String salt) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public User(String login, String passwordHash, String salt) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    /**
     * Gets the value of the id property.
     * @return текущее значение автоинкремента.
     */
    public int getId() {
        return id;
    }

    /**
     * @param value Sets the value of the id property.
     */
    public void setId(int value) {
        this.id = value;
    }

    /**
     * Gets the value of the login property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the value of the login property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogin(String value) {
        this.login = value;
    }

    /**
     * Gets the value of the passwordHash property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the value of the passwordHash property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPasswordHash(String value) {
        this.passwordHash = value;
    }

    /**
     * Gets the value of the salt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Sets the value of the salt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSalt(String value) {
        this.salt = value;
    }

    /**
     * @param password       Передаваемый пароль пользователя
     * @param isPasswordHash Указывает на то, зашифрован ли отправленный пароль.
     *                       Если false - пароль будет зашифрован
     *                       Если true - к паролю будет добавлена соль
     * @throws Exception     Ошибка при хешировании пароля
     */
    public void createPassword(String password, boolean isPasswordHash) throws Exception {
        SecureRandom sr = new SecureRandom();
        byte[] saltBytes = new byte[8];
        sr.nextBytes(saltBytes);

        this.salt = CryptoUtils.byteArrayToHexString(saltBytes);

        if(!isPasswordHash) {
            password = CryptoUtils.computeHashToHexString(password);
        }
        this.passwordHash = CryptoUtils.computeHashToHexString(password, salt);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }
}
