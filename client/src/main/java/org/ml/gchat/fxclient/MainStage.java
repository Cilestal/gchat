package org.ml.gchat.fxclient;

import org.ml.gchat.engine.Client;
import org.ml.gchat.managers.MessageManager;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Date: 20.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class MainStage extends Stage{

    public MainStage(Client client) {
        AnchorPane root = new AnchorPane();
        MainController controller = new MainController(client);

        //load view
        URL resource = getClass().getResource(MainController.CLIENT_FXML);
        ClientApplication.loadView(resource, controller, root);

        Image icon = new Image(getClass().getResource("/icons/client.png").toString());
        this.getIcons().add(icon);

        this.setTitle(MessageManager.getResourceBundle().getString("main_title"));
        this.setScene(new Scene(root));

        this.setOnCloseRequest(controller::onClose);
        this.centerOnScreen();
        this.show();
    }
}
