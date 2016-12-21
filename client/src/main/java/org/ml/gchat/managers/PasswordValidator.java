package org.ml.gchat.managers;

import java.util.regex.Pattern;

/**
 * Date: 22.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class PasswordValidator {
    private Pattern passwordPattern;
    private Pattern loginPattern;

    private static final String PASSWORD_PATTERN = "^[a-zA-Z\\w@#$%^&+=-]{4,16}$";
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,16}$";

    public PasswordValidator() {
        this.passwordPattern = Pattern.compile(PASSWORD_PATTERN);
        this.loginPattern = Pattern.compile(USERNAME_PATTERN);
    }

    public boolean validLogin(String login) {
        return loginPattern.matcher(login).matches();

    }

    public boolean validPass(String pass){
        return passwordPattern.matcher(pass).matches();
    }
}
