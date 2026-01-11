package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.controller.interfaces.ModeAwareController;
import at.ac.hcw.porty.dto.ScanConfigDTO;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class MainController {
    @FXML
    private BorderPane contentBorderPane;
    @FXML
    private ToggleButton simplicityModeSwitch;

    private ModeAwareController currentController;

    @FXML
    public void initialize() {
        simplicityModeSwitch.setOnAction(e -> {
            if (currentController == null) return;

            if (simplicityModeSwitch.isSelected()) {
                currentController.setAdvancedMode();
            } else {
                currentController.setSimpleMode();
            }
        });

        navigateToDashboard(null);
    }

    @FXML
    private void navigateToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/at/ac/hcw/porty/scenes/dashboard.fxml")
            );

            Parent view = loader.load();

            DashboardController dashboardController =
                    loader.getController();

            dashboardController.setMainController(this);

            contentBorderPane.setCenter(view);

            this.currentController = dashboardController;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToHistory(ActionEvent event) {
        handleNavigation("/at/ac/hcw/porty/scenes/history.fxml");
    }

    @FXML
    private void navigateToCredits(ActionEvent event) {
        handleNavigation("/at/ac/hcw/porty/scenes/credits.fxml");
    }

    @FXML
    public void navigateToResults() { handleNavigation("/at/ac/hcw/porty/scenes/results.fxml"); }

    private void handleNavigation(String route) {
        URL url = getClass().getResource(route);

        try {
            FXMLLoader loader = new FXMLLoader(url);
            contentBorderPane.setCenter(loader.load());

            Object controller = loader.getController();

            if (controller instanceof ModeAwareController modeAware) {
                currentController = modeAware;

                simplicityModeSwitch.setVisible(true);
                simplicityModeSwitch.setManaged(true);

                if (simplicityModeSwitch.isSelected()) {
                    modeAware.setAdvancedMode();
                } else {
                    modeAware.setSimpleMode();
                }
            } else {
                currentController = null;

                simplicityModeSwitch.setVisible(false);
                simplicityModeSwitch.setManaged(false);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
