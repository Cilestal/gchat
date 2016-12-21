package org.ml.gchat.engine;

import org.ml.gchat.db.IncorrectLoginException;
import org.ml.gchat.db.UserDB;
import org.ml.gchat.db.model.User;
import org.ml.gchat.util.CryptoUtils;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 13.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public final class CommonLoginLogic implements LoginLogic {
    private UserDB userDB;
    private static final int PASSWORD_HASH_LENGTH = 64;

    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,16}$";

    private Pattern loginPattern;

    private static final Logger LOG = Logger.getLogger(CommonLoginLogic.class);

    public CommonLoginLogic(UserDB userDB) {
        this.userDB = userDB;
        this.loginPattern = Pattern.compile(USERNAME_PATTERN);
    }

    @Override
    public boolean checkLogin(Message message) {
        String keyLogin = message.getUser();
        String keyPasswordHash = message.getMessage();

        if (keyLogin == null || keyPasswordHash == null) return false;

        try {
            if (keyPasswordHash.length() < PASSWORD_HASH_LENGTH) {
                keyPasswordHash = CryptoUtils.computeHashToHexString(keyPasswordHash);
            }

            User user = userDB.getUser(keyLogin);
            if (user == null) return false;

            String serverUserPassword = user.getPasswordHash();
            String salt = user.getSalt();

            //sha256(sha256(keyPasswordHash) + salt)
            String userPass = CryptoUtils.computeHashToHexString(keyPasswordHash, salt);
            return serverUserPassword.equals(userPass);
        } catch (Exception e) {
            LOG.error("Ошибка при использовании методов шифрования!");
            return false;
        }
    }

    @Override
    public void registerLogin(Message mes) throws Exception {
        String login = mes.getUser();
        String pass = mes.getMessage();

        Matcher loginMatcher = loginPattern.matcher(login);
        if(!loginMatcher.matches()) {
            throw new IncorrectLoginException("Недопустимые символы для логина.");
        }

        User user = new User(login);

        boolean hash = pass.length() == PASSWORD_HASH_LENGTH;
        user.createPassword(pass, hash);
        userDB.addUser(user);
    }
}
