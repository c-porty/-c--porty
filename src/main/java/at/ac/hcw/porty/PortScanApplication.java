package at.ac.hcw.porty;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class PortScanApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PortScanApplication.class.getResource("scenes/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1440, 960);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/at/ac/hcw/porty/styles/styles.css")).toExternalForm()
        );
        stage.setTitle("Porty - Portscanner");
        stage.setScene(scene);
        stage.show();
    }
}
