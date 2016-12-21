package org.ml.gchat.fxclient;

import org.ml.gchat.managers.ConfigurationManager;
import org.ml.gchat.managers.MessageManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

/**
 * Date: 18.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class ClientApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MessageManager.loadBundle(Locale.getDefault());

        //load configs
        ConfigurationManager cfm = loadConfig();

        //set icon
        Image icon = new Image(getClass().getResource("/icons/client.png").toString());
        primaryStage.getIcons().add(icon);

        //create root
        AnchorPane root = new AnchorPane();

        //create controller
        AuthController authController = new AuthController(cfm.getProperties(), root);

        //load view
        URL resource = getClass().getResource(AuthController.AUTH_FXML);
        ClientApplication.loadView(resource, authController, root);

        primaryStage.setTitle(MessageManager.getMessage("auth_title"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(authController::onClose);
        primaryStage.show();
    }

    public ConfigurationManager loadConfig(){
        ConfigurationManager cfm = new ConfigurationManager(ConfigurationManager.DEFAULT_CLIENT_CONFIG);
        cfm.load();
        return cfm;
    }

    public static void loadView(URL url, Object controller, Object root){
        FXMLLoader fxmlLoader = new FXMLLoader(url);
        fxmlLoader.setController(controller);
        fxmlLoader.setRoot(root);
        fxmlLoader.setResources(MessageManager.getResourceBundle());

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
