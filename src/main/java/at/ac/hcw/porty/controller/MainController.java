package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.utils.NetUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
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

    private DashboardController dashBoardController;
    private String currentRoute = "/at/ac/hcw/porty/scenes/dashboard.fxml";
    private String tracebackRoute = "/at/ac/hcw/porty/scenes/dashboard.fxml";

    @FXML
    public void initialize() {
        simplicityModeSwitch.setOnAction(e -> {
            if (dashBoardController == null) return;

            if (simplicityModeSwitch.isSelected()) {
                dashBoardController.setAdvancedMode();
            } else {
                dashBoardController.setSimpleMode();
            }
        });

        try {
            systemIPAddress.setText(NetUtils.getLanIPv4Address());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        navigateToDashboard(null);
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
