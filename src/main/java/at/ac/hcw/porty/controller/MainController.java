package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.utils.I18n;
import at.ac.hcw.porty.types.enums.Language;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.utils.NetUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.ToggleSwitch;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController {
    private static final Logger logger =
            LoggerFactory.getLogger(MainController.class);

    @FXML private BorderPane contentBorderPane;
    @FXML private ToggleButton simplicityModeSwitch;
    @FXML private Label systemIPAddress;
    @FXML private ToggleSwitch lightModeSwitch;
    @FXML private MenuButton languageMenu;
    @FXML private RadioMenuItem langDeItem;
    @FXML private RadioMenuItem langEnItem;

    @FXML private Button navHome;
    @FXML private Button navHistory;
    @FXML private Button navAboutUs;
    @FXML private Label yourIpAddress;
    @FXML private Label simpleOption;
    @FXML private Label advancedOption;

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

        setUpLanguageMenu();

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

    private void setUpLanguageMenu() {
        ToggleGroup tg = new ToggleGroup();
        langDeItem.setToggleGroup(tg);
        langEnItem.setToggleGroup(tg);
        if (I18n.getLanguage() == Language.DE) {
            langDeItem.setSelected(true);
        } else {
            langEnItem.setSelected(true);
        }
        langDeItem.setOnAction(e -> I18n.setLanguage(Language.DE));
        langEnItem.setOnAction(e -> I18n.setLanguage(Language.EN));
        languageMenu.textProperty().bind(Bindings.createStringBinding(
                () -> I18n.getLanguage() == Language.DE ? "DE" : "EN",
                I18n.languageProperty()
        ));

        bindLanguageTexts();
    }

    private void bindLanguageTexts() {
        navHome.textProperty().bind(I18n.bind("home"));
        navHistory.textProperty().bind(I18n.bind("history"));
        navAboutUs.textProperty().bind(I18n.bind("about-us"));

        simpleOption.textProperty().bind(I18n.bind("simple"));
        advancedOption.textProperty().bind(I18n.bind("advanced"));
        yourIpAddress.textProperty().bind(I18n.bind("your-ip-address"));

        langDeItem.textProperty().bind(I18n.bind("german"));
        langEnItem.textProperty().bind(I18n.bind("english"));
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
        ResourceBundle rb = I18n.getBundle();

        try {
            FXMLLoader loader = new FXMLLoader(url, rb);
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
