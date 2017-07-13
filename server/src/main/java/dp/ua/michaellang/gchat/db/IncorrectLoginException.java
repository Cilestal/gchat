package dp.ua.michaellang.gchat.db;

/**
 * Date: 22.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class IncorrectLoginException extends Exception{
    public IncorrectLoginException() {
    }

    public IncorrectLoginException(String message) {
        super(message);
    }

    public IncorrectLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectLoginException(Throwable cause) {
        super(cause);
    }

    public IncorrectLoginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
