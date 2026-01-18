package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.utils.NetUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.ToggleSwitch;
import org.kordamp.ikonli.javafx.FontIcon;
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

    private FontIcon switchIcon;

    private DashboardController dashBoardController;
    private String currentRoute = "/at/ac/hcw/porty/scenes/dashboard.fxml";
    private String tracebackRoute = "/at/ac/hcw/porty/scenes/dashboard.fxml";

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
            if (dashBoardController == null) return;

            if (simplicityModeSwitch.isSelected()) {
                dashBoardController.setAdvancedMode();
            } else {
                dashBoardController.setSimpleMode();
            }
        });

        lightModeSwitch.selectedProperty().addListener((obs, oldVal, isOn) -> {
            if (switchIcon != null) {
                switchIcon.setIconLiteral(
                        isOn ? "mdi2w-weather-sunny"
                                : "mdi2w-weather-night"
                );
            }
        });

        try {
            systemIPAddress.setText(NetUtils.getLanIPv4Address());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        Platform.runLater(() -> {
            StackPane thumb = (StackPane) lightModeSwitch.lookup(".thumb");
            thumb.getStyleClass().add("porty-switch-thumb-pane");

            switchIcon = new FontIcon("mdi2w-weather-night");
            switchIcon.getStyleClass().add("porty-switch-icon");

            thumb.getChildren().add(switchIcon);
        });

        navigateToDashboard(null);
    }

    public void setScene(Scene scene) {
        this.scene = scene;

        scene.getStylesheets().add(darkCss);
    }

    @FXML
    private void navigateToDashboard(ActionEvent event) {
        handleNavigation("/at/ac/hcw/porty/scenes/dashboard.fxml", null);
    }

    @FXML
    private void navigateToHistory(ActionEvent event) {
        handleNavigation("/at/ac/hcw/porty/scenes/history.fxml", null);
    }

    @FXML
    private void navigateToCredits(ActionEvent event) {
        handleNavigation("/at/ac/hcw/porty/scenes/credits.fxml", null);
    }

    @FXML
    public void navigateToResults(ScanSummary summary) {
        handleNavigation("/at/ac/hcw/porty/scenes/results.fxml", summary);
    }

    public void traceBackNavigation(){
        handleNavigation(tracebackRoute, null);
    }

    private void handleNavigation(String route, ScanSummary scanSummary) {
        URL url = getClass().getResource(route);
        tracebackRoute = currentRoute;

        try {
            FXMLLoader loader = new FXMLLoader(url);
            contentBorderPane.setCenter(loader.load());

            Object controller = loader.getController();
            if (controller instanceof ResultsController resultsController) {
                resultsController.setMainController(this);
                if (scanSummary != null) {
                    resultsController.setScanSummary(scanSummary);
                }
            }

            if (controller instanceof DashboardController dashboardController) {
                dashBoardController = dashboardController;
                dashboardController.setMainController(this);

                simplicityModeSwitch.setVisible(true);
                simplicityModeSwitch.setManaged(true);

                if (simplicityModeSwitch.isSelected()) {
                    dashboardController.setAdvancedMode();
                } else {
                    dashboardController.setSimpleMode();
                }
            } else {
                simplicityModeSwitch.setVisible(false);
                simplicityModeSwitch.setManaged(false);
            }

            currentRoute = route;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
