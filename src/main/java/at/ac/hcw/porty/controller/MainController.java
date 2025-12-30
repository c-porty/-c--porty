package at.ac.hcw.porty.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;

public class MainController {
    @FXML
    private BorderPane mainBorderPane;


    @FXML
    private void navigateToDashboard(ActionEvent event) {
        handleNavigation("/at/ac/hcw/porty/dashboard.fxml");
    }

    @FXML
    private void navigateToHistory(ActionEvent event) {
        handleNavigation("/at/ac/hcw/porty/history.fxml");
    }

    @FXML
    private void navigateToCredits(ActionEvent event) {
        handleNavigation("/at/ac/hcw/porty/credits.fxml");
    }

    private void handleNavigation(String route) {
        URL url = getClass().getResource(route);

        try {
            FXMLLoader loader = new FXMLLoader(url);
            mainBorderPane.setCenter(loader.load());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
