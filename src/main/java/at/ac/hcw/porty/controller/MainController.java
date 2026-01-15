package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.utils.NetUtils;
import at.ac.hcw.porty.types.interfaces.ModeAwareController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class MainController {
    private static final Logger logger =
            LoggerFactory.getLogger(MainController.class);

    @FXML
    private BorderPane contentBorderPane;
    @FXML
    private ToggleButton simplicityModeSwitch;
    @FXML
    private Label systemIPAddress;
    @FXML
    private ToggleSwitch lightModeSwitch;

    private ModeAwareController currentController;

    private Scene scene;

    private String lightCss;
    private String darkCss;

    @FXML
    public void initialize() {

        lightCss = getClass().getResource("/at/ac/hcw/porty/styles/styles_light.css").toExternalForm();
        darkCss  = getClass().getResource("/at/ac/hcw/porty/styles/styles_dark.css").toExternalForm();

        lightModeSwitch.selectedProperty().addListener((obs, oldVal, light) -> {
            if (scene != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(light ? lightCss : darkCss);
            }
        });

        simplicityModeSwitch.setOnAction(e -> {
            if (currentController == null) return;

            if (simplicityModeSwitch.isSelected()) {
                currentController.setAdvancedMode();
            } else {
                currentController.setSimpleMode();
            }
        });

        try {
            systemIPAddress.setText(NetUtils.getLanIPv4Address());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        navigateToDashboard(null);
    }

    public void setScene(Scene scene) {
        this.scene = scene;

        scene.getStylesheets().add(darkCss);
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

            simplicityModeSwitch.setVisible(true);
            simplicityModeSwitch.setManaged(true);

            if (simplicityModeSwitch.isSelected()) {
                dashboardController.setAdvancedMode();
            } else {
                dashboardController.setSimpleMode();
            }

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
