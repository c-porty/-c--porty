package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.dto.ScanConfigDTO;
import at.ac.hcw.porty.listeners.PortScanCLIListener;
import at.ac.hcw.porty.listeners.PortScanUIListener;
import at.ac.hcw.porty.scanner.Scanner;
import at.ac.hcw.porty.scanner.ScannerFactory;
import at.ac.hcw.porty.types.interfaces.MainAwareController;
import at.ac.hcw.porty.utils.AlertManager;
import at.ac.hcw.porty.utils.Confetti;
import at.ac.hcw.porty.utils.I18n;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.NmapOptions;
import at.ac.hcw.porty.types.records.PortRange;
import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.enums.ScanStrategy;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ListChangeListener;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import javafx.util.Duration;

public class DashboardController implements MainAwareController {
    @FXML private Button startScanButton;
    @FXML private ListView<String> scanProgressConsole;
    @FXML private TextField ipTextField;
    @FXML private VBox advancedOptionControl;
    @FXML private Label descriptorLabel;
    @FXML private CheckBox serviceDetectionCheckbox;
    @FXML private CheckBox osDetectionCheckbox;
    @FXML private CheckBox tcpConnectScanCheckbox;
    @FXML private CheckBox synScanCheckbox;
    @FXML private TextField timeoutTextField;
    @FXML private TextField statsEveryTextField;
    @FXML private CheckBox saveScanCheckbox;
    @FXML private CheckBox ipMaskCheckbox;
    @FXML private TextField ipMaskTextField;
    @FXML private ProgressIndicator scanProgressIndicator;
    @FXML private Label dashboardTitle;
    @FXML private Label scanLabel;
    @FXML private CheckBox portRangeCheckbox;
    @FXML private TextField portRangeStartTextField;
    @FXML private TextField portRangeEndTextField;
    @FXML private Label portRangeConnector;
    @FXML private Label scanProgressPercentage;
    @FXML private CheckBox saveConfigCheckbox;
    @FXML private StackPane configFileField;
    @FXML private TitledPane advancedOptionTitledPane;
    @FXML private Label configFileFieldLabel;
    @FXML private Tooltip resultSaveTooltip;
    @FXML private CheckBox udpScanCheckbox;
    @FXML private TitledPane scanProgressConsoleTitledPane;
    @FXML private Pane confettiPane;

    private MainController mainController;

    private static final Logger logger =
            LoggerFactory.getLogger(HistoryController.class);

    ObservableList<String> consoleLines = FXCollections.observableArrayList();

    private boolean onScan = false;
    private boolean advancedOptions = false;
    private ScanHandle handle;

    private ScanConfigDTO scanConfigDTO;

    @FXML
    public void initialize() {
        scanConfigDTO = new ScanConfigDTO();
        scanProgressConsole.setItems(consoleLines);

        setupLanguageTexts();

        consoleLines.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    scanProgressConsole.scrollTo(consoleLines.size() - 1);
                }
            }
        });

        tcpConnectScanCheckbox.selectedProperty().addListener((ov, oldValue, newValue) -> {
            if(newValue && synScanCheckbox.isSelected()){
                synScanCheckbox.selectedProperty().set(false);
            }
        });

        synScanCheckbox.selectedProperty().addListener((ov, oldValue, newValue) -> {
            if(newValue && tcpConnectScanCheckbox.isSelected()){
                tcpConnectScanCheckbox.selectedProperty().set(false);
            }
        });

        scanProgressIndicator.setProgress(0.0);

        scanProgressIndicator.progressProperty().addListener((ov, oldValue, progress) -> {
            if (progress.doubleValue() >= 1.0) {
                scanProgressPercentage.textProperty().bind(I18n.bind("dashboard.scan.done"));
            } else {
                scanProgressPercentage.setText((int) (progress.doubleValue() * 100) + "%");
            }
        });

        configFileField.visibleProperty()
                .bind(advancedOptionTitledPane.expandedProperty().not());
        configFileField.managedProperty()
                .bind(configFileField.visibleProperty());

        configFileField.setOnDragOver(event -> {
            if (event.getGestureSource() != configFileField &&
                    event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        configFileField.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                List<File> files = db.getFiles();
                for (File file : files) {
                    if (file.getName().endsWith(".json")) {
                        loadConfig(file);
                    }else{
                        Alert alert = AlertManager.createErrorAlert(I18n.bind("dashboard.configFile.load.error").get());
                        alert.showAndWait();
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        TextFormatter<Long> longFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty()) {
                return change;
            }

            try {
                Long.parseLong(newText);
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        });

        TextFormatter<Double> doubleFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty()) {
                return change;
            }

            try {
                Double.parseDouble(newText);
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        });

        TextFormatter<Integer> intFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty()) {
                return change;
            }

            try {
                Integer.parseInt(newText);
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        });

        TextFormatter<Integer> portRangeStartFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty()) {
                return change;
            }

            try {
                Integer.parseInt(newText);
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        });

        TextFormatter<Integer> portRangeEndFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty()) {
                return change;
            }

            try {
                Integer.parseInt(newText);
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        });


        ipMaskTextField.disableProperty().bind(ipMaskCheckbox.selectedProperty().not());

        portRangeStartTextField.disableProperty().bind(portRangeCheckbox.selectedProperty().not());
        portRangeEndTextField.disableProperty().bind(portRangeCheckbox.selectedProperty().not());

        PseudoClass INACTIVE = PseudoClass.getPseudoClass("inactive");
        portRangeConnector.pseudoClassStateChanged(INACTIVE, true);
        portRangeCheckbox.selectedProperty().addListener((obs, oldVal, selected) -> {
            portRangeConnector.pseudoClassStateChanged(
                    INACTIVE,
                    !selected
            );
        });

        timeoutTextField.setTextFormatter(longFormatter);
        statsEveryTextField.setTextFormatter(doubleFormatter);
        ipMaskTextField.setTextFormatter(intFormatter);
        portRangeStartTextField.setTextFormatter(portRangeStartFormatter);
        portRangeEndTextField.setTextFormatter(portRangeEndFormatter);

        setSimpleMode();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    protected void onScanStartButtonClick() {
        if(!onScan) {
            if(!ipTextField.textProperty().get().isEmpty()) {
                scanProgressPercentage.textProperty().unbind();
                setProgress(0.0);
                scanProgressPercentage.setText("0%");
                if (advancedOptions) {
                    advancedScan();
                } else {
                    simpleScan();
                }
            }else{
                Alert alert = AlertManager.createErrorAlert(I18n.bind("dashboard.ipField.error").get());
                alert.showAndWait();
            }
        } else{
            scanProgressPercentage.textProperty().unbind();
            setProgress(0.0);
            if(handle != null) {
                handle.cancel();
            }
            startScanButton.setStyle("-fx-background-color: -porty-secondary;");
            startScanButton.setText("Start Scan");
            onScan=false;
        }
    }

    public void setSimpleMode(){
        advancedOptionControl.setVisible(false);
        advancedOptionControl.setManaged(false);
        descriptorLabel.textProperty().unbind();
        descriptorLabel.textProperty().bind(I18n.bind("dashboard.quickScan"));
        advancedOptions = false;
    }

    public void setAdvancedMode(){
        advancedOptionControl.setVisible(true);
        advancedOptionControl.setManaged(true);
        advancedOptionTitledPane.expandedProperty().set(false);
        descriptorLabel.textProperty().unbind();
        descriptorLabel.textProperty().bind(I18n.bind("dashboard.advancedScan"));
        advancedOptions = true;
    }

    protected void scan(NmapOptions options, PortRange range){
        if(!scanConfigDTO.getHost().isEmpty() && scanConfigDTO.getHost()!=null) {
            startScanButton.setStyle("-fx-background-color: red;");
            startScanButton.textProperty().unbind();
            startScanButton.textProperty().bind(I18n.bind("dashboard.stopScan"));
            onScan = true;
            new Thread(() -> {
                handle = scanHandleGenerator(new Host(scanConfigDTO.getHost(), scanConfigDTO.isIncludeSubnetMask()? scanConfigDTO.getSubnetMask(): null), range, options);
                handle.summary().join();
                Platform.runLater(() -> {
                    startScanButton.setStyle("-fx-background-color: -porty-secondary;");
                    startScanButton.textProperty().unbind();
                    startScanButton.textProperty().bind(I18n.bind("dashboard.startScan"));
                    onScan = false;
                });
            }).start();
        }
    }

    protected ScanHandle scanHandleGenerator(Host host, PortRange range, NmapOptions options){
        ScanConfig config = new ScanConfig(host, range, options);
        Scanner scanner = new Scanner(ScannerFactory.create(ScanStrategy.NMAP));
        // both the CLI listener and the UI listener, UI listener is for actual frontend, CLI only for debugging
        PortScanListener[] listeners = { new PortScanUIListener(consoleLines, mainController, this), new PortScanCLIListener() };

        return scanner.scan(config, listeners);
    }

    protected void simpleScan(){
        scanConfigDTO.setHost(ipTextField.getText());
        NmapOptions options = new NmapOptions(saveScanCheckbox.isSelected());
        scan(options, new PortRange(-1,-1));
    }

    protected void advancedScan() {
        setDTO();
        NmapOptions options = new NmapOptions(
                scanConfigDTO.isServiceDetection(),
                scanConfigDTO.isOsDetection(),
                scanConfigDTO.isTcpConnectScan(),
                scanConfigDTO.isSynScan(),
                scanConfigDTO.getHostTimeout(),
                scanConfigDTO.getStatsEvery(),
                saveScanCheckbox.isSelected(),
                scanConfigDTO.isIncludeSubnetMask(),
                scanConfigDTO.isUdpScan()
        );

        if(saveConfigCheckbox.isSelected()){
            saveConfig(options);
        }

        scan(options, scanConfigDTO.getPortRange());
    }

    public void setProgress(double percent){
        scanProgressIndicator.setProgress(percent/100);
    }

    protected void setDTO() {
        scanConfigDTO.setHost(ipTextField.getText());
        scanConfigDTO.setServiceDetection(serviceDetectionCheckbox.isSelected());
        scanConfigDTO.setOsDetection(osDetectionCheckbox.isSelected());
        scanConfigDTO.setTcpConnectScan(tcpConnectScanCheckbox.isSelected());
        scanConfigDTO.setSynScan(synScanCheckbox.isSelected());
        scanConfigDTO.setUdpScan(udpScanCheckbox.isSelected());

        if(!statsEveryTextField.getText().isEmpty() && Double.parseDouble(statsEveryTextField.getText())>0) {
            scanConfigDTO.setStatsEvery(Double.parseDouble(statsEveryTextField.getText()));
        } else{
            scanConfigDTO.setStatsEvery(2);
        }

        if(!timeoutTextField.getText().isEmpty() && Double.parseDouble(timeoutTextField.getText())>0) {
            scanConfigDTO.setHostTimeout(Long.parseLong(timeoutTextField.getText()));
        } else{
            scanConfigDTO.setHostTimeout(-1);
        }

        if(ipMaskCheckbox.isSelected()){
            scanConfigDTO.setIncludeSubnetMask(true);
            scanConfigDTO.setSubnetMask(!ipMaskTextField.getText().isEmpty()? Integer.parseInt(ipMaskTextField.getText()): null );
        } else{
            scanConfigDTO.setIncludeSubnetMask(false);
        }

        if(portRangeCheckbox.isSelected()){
            scanConfigDTO.setPortRange(!portRangeStartTextField.getText().isEmpty()?Integer.parseInt(portRangeStartTextField.getText()): -1,
                    !portRangeEndTextField.getText().isEmpty()?Integer.parseInt(portRangeEndTextField.getText()): -1);
        } else{
            scanConfigDTO.setPortRange(new PortRange(-1,-1));
        }
    }

    @FXML
    private void handleFileFieldDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            searchForConfigFile();
        }
    }

    public void saveConfig(NmapOptions options) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            Host host=new Host(ipTextField.getText(), scanConfigDTO.getSubnetMask());
            PortRange portRange= scanConfigDTO.getPortRange();

            ScanConfig config = new ScanConfig(host, portRange, options);

            String dirPath = "src/main/saves/configs";
            File dir = new File(dirPath);

            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    logger.error("Folder could not be created!");
                }
            }

            File file = pickSaveLocation(host.address()+"-"+Instant.now().getEpochSecond() +"-scanConfig");
            if(file!=null) {
                mapper.writeValue(file, config);
                logger.info("Config File saved");
            }else{
                Alert alert = AlertManager.createErrorAlert(I18n.bind("dashboard.configFile.save.error").get());
                alert.showAndWait();
                logger.error("No File chosen");
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void loadConfig(File file) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            ScanConfig scanConfig = mapper.readValue(file, ScanConfig.class);
            logger.info("Config loaded: "+scanConfig);
            setOptionsFromConfig(scanConfig);
            Alert alert = AlertManager.createInfoAlert(I18n.bind("dashboard.configFile.load.success").get());
            alert.showAndWait();
        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            Alert alert = AlertManager.createErrorAlert(I18n.bind("dashboard.configFile.load.error").get());
            alert.showAndWait();
            logger.error("Wrong file provided!");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private File pickSaveLocation(String filename){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.bind("dashboard.configFile.save.finder.title").get());

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.bind("dashboard.configFile.load.finder.prompt").get(), "*.json")
        );

        File startDir = new File("src/main/saves/configs");
        if (startDir.exists()) {
            fileChooser.setInitialDirectory(startDir);
        }

        fileChooser.setInitialFileName(filename+".json");

        Stage stage = mainController.getStage();

        return fileChooser.showSaveDialog(stage);
    }

    private void searchForConfigFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.bind("dashboard.configFile.load.finder.title").get());

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.bind("dashboard.configFile.load.finder.prompt").get(), "*.json")
        );

        File startDir = new File("src/main/saves/configs");
        if (startDir.exists()) {
            fileChooser.setInitialDirectory(startDir);
        }

        Stage stage = mainController.getStage();

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            loadConfig(file);
        }
    }

    private void setOptionsFromConfig(ScanConfig config){
        ipTextField.textProperty().set(config.host().address());
        serviceDetectionCheckbox.selectedProperty().set(config.options().serviceDetection());
        osDetectionCheckbox.selectedProperty().set(config.options().osDetection());
        tcpConnectScanCheckbox.selectedProperty().set(config.options().tcpConnectScan());
        synScanCheckbox.selectedProperty().set(config.options().synScan());
        udpScanCheckbox.selectedProperty().set(config.options().udpScan());
        timeoutTextField.textProperty().set(config.options().hostTimeout()+"");
        statsEveryTextField.textProperty().set(config.options().statsEvery()+"");
        ipMaskCheckbox.selectedProperty().set(config.host().subnet()!=null);
        ipMaskTextField.textProperty().set(config.host().subnet()+"");
        portRangeCheckbox.selectedProperty().set((config.range().start()!=-1)||(config.range().end()!=-1));
        portRangeStartTextField.textProperty().set(config.range().start()+"");
        portRangeEndTextField.textProperty().set(config.range().end()+"");
    }

    private void setupLanguageTexts() {
        ipTextField.promptTextProperty().bind(I18n.bind("enter-ip-address"));
        dashboardTitle.textProperty().bind(I18n.bind("dashboard.title"));
        scanLabel.textProperty().bind(I18n.bind("dashboard.scanLabel"));

        ipTextField.promptTextProperty().bind(I18n.bind("dashboard.enter-ip-address"));
        timeoutTextField.promptTextProperty().bind(I18n.bind("dashboard.timeout"));
        statsEveryTextField.promptTextProperty().bind(I18n.bind("dashboard.statsEvery"));
        ipMaskTextField.promptTextProperty().bind(I18n.bind("dashboard.cidrSubnetMask"));
        portRangeStartTextField.promptTextProperty().bind(I18n.bind("dashboard.portRange-start"));
        portRangeEndTextField.promptTextProperty().bind(I18n.bind("dashboard.portRange-end"));

        saveScanCheckbox.textProperty().bind(I18n.bind("dashboard.saveResult"));
        serviceDetectionCheckbox.textProperty().bind(I18n.bind("dashboard.scanServices"));
        osDetectionCheckbox.textProperty().bind(I18n.bind("dashboard.scanOs"));
        tcpConnectScanCheckbox.textProperty().bind(I18n.bind("dashboard.enableTcpConnectScan"));
        synScanCheckbox.textProperty().bind(I18n.bind("dashboard.enableSynScan"));
        udpScanCheckbox.textProperty().bind(I18n.bind("dashboard.enableUdpScan"));
        ipMaskCheckbox.textProperty().bind(I18n.bind("dashboard.includeSubnetMask"));
        portRangeCheckbox.textProperty().bind(I18n.bind("dashboard.setPortRange"));
        saveConfigCheckbox.textProperty().bind(I18n.bind("dashboard.saveConfig"));

        configFileFieldLabel.textProperty().bind(I18n.bind("dashboard.configFile"));

        startScanButton.textProperty().bind(I18n.bind("dashboard.startScan"));

        resultSaveTooltip.textProperty().bind(I18n.bind("tooltip.result-save"));
        resultSaveTooltip.setShowDelay(Duration.millis(100));

        advancedOptionTitledPane.textProperty().bind(I18n.bind("dashboard.advancedOptions"));
        scanProgressConsoleTitledPane.textProperty().bind(I18n.bind("dashboard.console"));
    }

    public void celebrateSuccess() {
        if (confettiPane == null) return;

        double width = confettiPane.getWidth();
        double height = confettiPane.getHeight();

        int numberOfConfetti = 300;
        for (int i = 0; i < numberOfConfetti; i++) {
            Color color = Color.color(Math.random(), Math.random(), Math.random());
            Confetti confetti = new Confetti(color, width, height);
            confettiPane.getChildren().add(confetti);
            confetti.animate();

            confetti.opacityProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() <= 0.0) {
                    confettiPane.getChildren().remove(confetti);
                }
            });
        }
    }
}
