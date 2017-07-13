package dp.ua.michaellang.gchat.fxclient;

import dp.ua.michaellang.gchat.engine.Message;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import dp.ua.michaellang.gchat.engine.Client;
import dp.ua.michaellang.gchat.managers.MessageManager;

import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;

/**
 * Date: 21.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class MainController implements Initializable{
    public static final String CLIENT_FXML = "/sample/client.fxml";

    private Client client;
    private Timer timer;

    @FXML
    private TextField messageField;

    @FXML
    private ListView<Message> messageBox;

    public MainController(Client client) {
        this.client = client;
        this.timer = new Timer("MessageReader");
    }

    @FXML
    void sendMessage(ActionEvent event) {
        client.sendPublicMassage(messageField.getText());
        messageField.setText("");
    }

    @FXML
    void changeLanguage(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        switch (source.getId()) {
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
        // TODO: 22.10.2016 UpdateText
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> readMessages()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }


    private void readMessages(){
        Iterator<Message> iterator = client.getMessages().iterator();

        while (iterator.hasNext()){
            Message next = iterator.next();
            messageBox.getItems().add(next);
            iterator.remove();
        }
    }

    public void onClose(WindowEvent event) {
        if (client != null && client.isRunning())
            this.client.close();

        timer.cancel();
    }
}
