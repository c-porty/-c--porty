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

import java.time.Duration;

public class DashboardController {
    @FXML
    private Button startScanButton;
    @FXML
    private ListView<String> scanProgressConsole;
    @FXML
    private TextField ipTextField;
    @FXML
    private ToggleButton simplicityModeSwitch;
    @FXML
    private VBox advancedOptionControl;
    @FXML
    private Label descriptorLabel;

    ObservableList<String> consoleLines = FXCollections.observableArrayList();

    private boolean onScan = false;
    private boolean advancedOptions = false;
    private ScanHandle handle;

    private ScanConfigDTO scanConfigDTO;

    @FXML
    public void initialize() {
        scanConfigDTO = new ScanConfigDTO("");
        scanProgressConsole.setItems(consoleLines);

        consoleLines.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    scanProgressConsole.scrollTo(consoleLines.size() - 1);
                }
            }
        });

        //Control for advanced mode
        advancedOptionControl.setVisible(false);
        advancedOptionControl.setManaged(false);
        descriptorLabel.setText("Quick Device Scan");

        simplicityModeSwitch.setOnAction(e -> {
            if (simplicityModeSwitch.isSelected()) {
                advancedOptionControl.setVisible(true);
                advancedOptionControl.setManaged(true);
                descriptorLabel.setText("Scan with preferences");
                advancedOptions = true;
            } else {
                advancedOptionControl.setVisible(false);
                advancedOptionControl.setManaged(false);
                descriptorLabel.setText("Quick Device Scan");
                advancedOptions = false;
            }
        });
    }

    @FXML
    protected void onScanStartButtonClick() throws InterruptedException {
        scanConfigDTO.setHost(ipTextField.getText());
        if(!onScan) {
            if(!scanConfigDTO.getHost().isEmpty() && scanConfigDTO.getHost()!=null) {
                startScanButton.setText("Stop");
                onScan = true;
                new Thread(() -> {
                    // as long as we do not have input for the port range simply use all available ports
                    handle = scan(new Host(scanConfigDTO.getHost()), new PortRange(-1, -1));
                    handle.summary().join();
                    Platform.runLater(() -> {
                        startScanButton.setText("Start Scan");
                        onScan = false;
                    });
                }).start();
            }
        }else{
            if(handle != null) {
                handle.cancel();
            }
            startScanButton.setText("Start Scan");
            onScan=false;
        }
    }

    protected ScanHandle scan(Host host, PortRange range){
        NmapOptions options = new NmapOptions();
        ScanConfig config = new ScanConfig(host, range, options);
        Scanner scanner = new Scanner(ScannerFactory.create(ScanStrategy.NMAP));
        // both the CLI listener and the UI listener, UI listener is for actual frontend, CLI only for debugging
        PortScanListener[] listeners = { new PortScanUIListener(consoleLines), new PortScanCLIListener() };

        return scanner.scan(config, listeners);
    }
}
