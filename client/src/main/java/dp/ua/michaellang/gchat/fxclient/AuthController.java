package dp.ua.michaellang.gchat.fxclient;

import dp.ua.michaellang.gchat.engine.Client;
import dp.ua.michaellang.gchat.engine.Command;
import dp.ua.michaellang.gchat.engine.ErrorMessage;
import dp.ua.michaellang.gchat.engine.Message;
import dp.ua.michaellang.gchat.managers.MessageManager;
import dp.ua.michaellang.gchat.managers.PasswordValidator;
import dp.ua.michaellang.gchat.util.CryptoUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Date: 18.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class AuthController {
    public static final String AUTH_FXML = "/sample/client_auth.fxml";

    private Client client;
    private Pane root;
    private PasswordValidator pv;

    private String errorFieldMessage;

    @FXML
    private Menu menu;
    @FXML
    private Text loginText;
    @FXML
    private Text passwordText;
    @FXML
    private Text errorField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField loginField;
    @FXML
    private Button authButton;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Button registerButton;

    public AuthController(Properties cfm, Pane root) {
        String host = cfm.getProperty("host");
        int port = Integer.parseInt(cfm.getProperty("port"));
        this.client = new Client(host, port);
        this.root = root;
        this.pv = new PasswordValidator();
    }

    private boolean readLoginAndPassword(boolean valid) {
        final String login = loginField.getText();
        String pass = passwordField.getText();

        if (valid) {
            if (!pv.validLogin(login)) {
                errorField.setText(MessageManager.getMessage("REGISTER_LOGIN_INCORRECT"));
                return false;
            }

            if (!pv.validPass(pass)) {
                errorField.setText(MessageManager.getMessage("REGISTER_PASSWORD_INCORRECT"));
                return false;
            }
        }

        try {
            pass = CryptoUtils.computeHashToHexString(pass);
            this.client.setUserName(login);
            this.client.setPassword(pass);

            pass = null;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @FXML
    void buttonSignUpAction(ActionEvent event) {
        if (!readLoginAndPassword(true)) return;
        hideElements();

        Runnable register = () -> {
            try {
                client.connect();   //trying to connect
                client.register();  //register user
                client.run();
            } catch (Exception e) {
                throw new RuntimeException("Runtime Exception!");
            }
        };

        readMessageThread(register);
    }

    @FXML
    void buttonSignInAction(ActionEvent event) {
        readLoginAndPassword(false);
        hideElements();

        Runnable auth = () -> {
            try {
                client.connect();   //trying to connect
                client.auth();
                client.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        readMessageThread(auth);
    }

    public void readMessageThread(Runnable runnable) {
        new Thread(() -> {
            new Thread(runnable, "ClientThread").start();

            try {
                while (readMessage()) {
                    Thread.sleep(250);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            showElements();
        }, "ReadMessageThread").start();
    }

    private boolean readMessage() {
        ConcurrentLinkedQueue<Message> messages = client.getMessages();
        for (Message mes : messages) {
            messages.remove(mes);

            switch (mes.getCommand()) {
                case AUTH_ACCEPT:
                    Platform.runLater(() -> {
                        new MainStage(client);
                        Stage primary = (Stage) root.getScene().getWindow();
                        primary.close();
                    });
                    errorField.setText("");
                    return false;
                case ERROR:
                    String message = mes.getMessage();
                    this.errorFieldMessage = ErrorMessage.valueOf(message).toString();
                    message = MessageManager.getMessage(errorFieldMessage);
                    errorField.setText(message);

                    passwordField.setText("");
                    return false;
                case REGISTER_ACCEPT:
                    errorFieldMessage = Command.REGISTER_ACCEPT.toString();
                    errorField.setText(MessageManager.getMessage(errorFieldMessage));
                    client.close();
                    return false;
            }
        }

        return true;
    }

    private void hideElements() {
        showElements(true, false);
    }

    private void showElements() {
        showElements(false, true);
    }

    private void showElements(boolean elem, boolean error) {
        errorField.setVisible(error);

        progressIndicator.setVisible(elem);
        authButton.setDisable(elem);
        registerButton.setDisable(elem);
    }

    @FXML
    void changeLanguage(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String menuButtonId = source.getId();

        switch (menuButtonId) {
            case "RU":
                MessageManager.loadBundle(new Locale("ru"));
                break;
            case "EN":
            default:
                MessageManager.loadBundle(Locale.ROOT);
                break;
        }
        updateText();
    }

    private void updateText() {
        this.loginText.setText(MessageManager.getMessage("login_text"));
        this.passwordText.setText(MessageManager.getMessage("password_text"));
        this.authButton.setText(MessageManager.getMessage("auth_button"));
        this.registerButton.setText(MessageManager.getMessage("register_button"));
        this.menu.setText(MessageManager.getMessage("language_menu"));

        if(errorFieldMessage != null){
            this.errorField.setText(MessageManager.getMessage(errorFieldMessage));
        }
    }

    public void onClose(WindowEvent event) {
        if (client != null && client.isRunning())
            this.client.close();
    }
}
