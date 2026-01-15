package at.ac.hcw.porty;

import at.ac.hcw.porty.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class PortScanApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PortScanApplication.class.getResource("scenes/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1440, 960);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/at/ac/hcw/porty/images/logo_32x32.png"))));
        stage.setTitle("Porty - Portscanner");

        MainController controller = fxmlLoader.getController();
        controller.setScene(scene);

        stage.setScene(scene);
        stage.show();

        // this is needed to stop ALL threads as soon as the application is closed
        stage.setOnCloseRequest(evt -> {
            Platform.exit();    // stop the JavaFX application
            System.exit(0); // stop all associated threads and exit the process with status 0
        });
    }
}
