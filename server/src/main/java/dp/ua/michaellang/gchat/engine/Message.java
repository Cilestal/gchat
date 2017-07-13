package dp.ua.michaellang.gchat.engine;

import java.util.Date;

/**
 * Date: 11.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public final class Message{
    /** Значение для хранения команды */
    private Command command;

    private String user;
    private Date date;
    private String message;

    public Message() {
    }

    public Message(Command command, String user, Date date, String message) {
        this.command = command;
        this.user = user;
        this.date = date;
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public Command getCommand() {
        return command;
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message1 = (Message) o;

        if (command != message1.command) return false;
        if (user != null ? !user.equals(message1.user) : message1.user != null) return false;
        if (date != null ? !date.equals(message1.date) : message1.date != null) return false;
        return message != null ? message.equals(message1.message) : message1.message == null;

    }

    @Override
    public int hashCode() {
        int result = command != null ? command.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
                "command=" + command +
                ", user='" + user + '\'' +
                ", date=" + date +
                ", message='" + message + '\'' +
                '}';
    }
}
