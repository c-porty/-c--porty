package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.dto.ScanConfigDTO;
import at.ac.hcw.porty.listeners.PortScanCLIListener;
import at.ac.hcw.porty.listeners.PortScanUIListener;
import at.ac.hcw.porty.scanner.Scanner;
import at.ac.hcw.porty.scanner.ScannerFactory;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.NmapOptions;
import at.ac.hcw.porty.types.records.PortRange;
import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.enums.ScanStrategy;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.VBox;

public class DashboardController {
    @FXML
    private Button startScanButton;
    @FXML
    private ListView<String> scanProgressConsole;
    @FXML
    private TextField ipTextField;
    @FXML
    private VBox advancedOptionControl;
    @FXML
    private Label descriptorLabel;
    @FXML
    private CheckBox serviceDetectionCheckbox;
    @FXML
    private CheckBox osDetectionCheckbox;
    @FXML
    private CheckBox tcpConnectScanCheckbox;
    @FXML
    private CheckBox synScanCheckbox;
    @FXML
    private TextField timeoutTextField;
    @FXML
    private TextField statsEveryTextField;
    @FXML
    private CheckBox saveScanCheckbox;
    @FXML
    private CheckBox ipMaskCheckbox;
    @FXML
    private TextField ipMaskTextField;
    @FXML
    private ProgressIndicator scanProgressIndicator;

    private MainController mainController;

    ObservableList<String> consoleLines = FXCollections.observableArrayList();

    private boolean onScan = false;
    private boolean advancedOptions = false;
    private ScanHandle handle;

    private ScanConfigDTO scanConfigDTO;

    @FXML
    public void initialize() {
        scanConfigDTO = new ScanConfigDTO();
        scanProgressConsole.setItems(consoleLines);

        consoleLines.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    scanProgressConsole.scrollTo(consoleLines.size() - 1);
                }
            }
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

        ipMaskTextField.disableProperty().bind(ipMaskCheckbox.selectedProperty().not());

        timeoutTextField.setTextFormatter(longFormatter);
        statsEveryTextField.setTextFormatter(doubleFormatter);
        ipMaskTextField.setTextFormatter(intFormatter);

        setSimpleMode();
        setProgress(0.0);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    protected void onScanStartButtonClick() throws InterruptedException {
        setProgress(0.0);
        if(!onScan) {

            if (advancedOptions) {
                advancedScan();
            } else {
                simpleScan();
            }
        } else{
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
        descriptorLabel.setText("Quick Device Scan");
        advancedOptions = false;
    }

    public void setAdvancedMode(){
        advancedOptionControl.setVisible(true);
        advancedOptionControl.setManaged(true);
        descriptorLabel.setText("Scan with preferences");
        advancedOptions = true;
    }

    protected void scan(NmapOptions options){
        if(!scanConfigDTO.getHost().isEmpty() && scanConfigDTO.getHost()!=null) {
            startScanButton.setStyle("-fx-background-color: red;");
            startScanButton.setText("Stop");
            onScan = true;
            new Thread(() -> {
                handle = scanHandleGenerator(new Host(scanConfigDTO.getHost(), scanConfigDTO.isIncludeSubnetMask()? scanConfigDTO.getSubnetMask(): null), new PortRange(-1, -1), options);
                handle.summary().join();
                Platform.runLater(() -> {
                    startScanButton.setStyle("-fx-background-color: -porty-secondary;");
                    startScanButton.setText("Start Scan");
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
        scan(options);
    }

    protected void advancedScan(){
        setDTO();
        NmapOptions options = new NmapOptions(
                scanConfigDTO.isServiceDetection(),
                scanConfigDTO.isOsDetection(),
                scanConfigDTO.isTcpConnectScan(),
                scanConfigDTO.isSynScan(),
                scanConfigDTO.getHostTimeout(),
                scanConfigDTO.getStatsEvery(),
                saveScanCheckbox.isSelected(),
                scanConfigDTO.isIncludeSubnetMask()
        );
        scan(options);
    }

    public void setProgress(double percent){
        scanProgressIndicator.setProgress(percent/100);
    }

    protected void setDTO(){
        scanConfigDTO.setHost(ipTextField.getText());
        scanConfigDTO.setServiceDetection(serviceDetectionCheckbox.isSelected());
        scanConfigDTO.setOsDetection(osDetectionCheckbox.isSelected());
        scanConfigDTO.setTcpConnectScan(tcpConnectScanCheckbox.isSelected());
        scanConfigDTO.setSynScan(synScanCheckbox.isSelected());

        if(!statsEveryTextField.getText().isEmpty() && Double.parseDouble(statsEveryTextField.getText())>0) {
            scanConfigDTO.setStatsEvery(Double.parseDouble(statsEveryTextField.getText()));
        } else{
            scanConfigDTO.setStatsEvery(-1);
        }

        if(!timeoutTextField.getText().isEmpty() && Double.parseDouble(timeoutTextField.getText())>0) {
            scanConfigDTO.setHostTimeout(Long.parseLong(timeoutTextField.getText()));
        } else{
            scanConfigDTO.setHostTimeout(-1);
        }

        if(ipMaskCheckbox.isSelected()){
            scanConfigDTO.setIncludeSubnetMask(true);
            scanConfigDTO.setSubnetMask(!ipMaskTextField.getText().isEmpty()? Integer.parseInt(ipMaskTextField.getText()): null );
        }
        else{
            scanConfigDTO.setIncludeSubnetMask(false);
        }
    }
}
