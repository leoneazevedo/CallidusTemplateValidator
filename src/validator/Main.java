/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validator;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import view.LayoutController;

/**
 *
 * @author humberto.leone
 */
public class Main extends Application {

    public static BorderPane Layout;
    public static Scene cena;
    public static Stage palco;

    @Override
    public void start(final Stage palco) throws IOException, InvalidFormatException {

        try {
            palco.setTitle("Template Validator");

            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getResource("/view/Layout.fxml"));

            Layout = (BorderPane) loader.load();

            LayoutController controller = loader.getController();
            controller.init(palco);

            cena = new Scene(Layout);
            palco.setScene(cena);
            palco.setResizable(false);
            palco.getIcons().add(new Image(getClass().getResourceAsStream("/source/favicon-32x32.png")));
            palco.centerOnScreen();
            palco.show();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

}
