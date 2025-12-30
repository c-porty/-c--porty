package at.ac.hcw.porty;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PortScanApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PortScanApplication.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 640);
        stage.setTitle("Porty - Portscanner");
        stage.setScene(scene);
        stage.show();
    }
}
