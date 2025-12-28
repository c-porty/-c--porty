package at.ac.hcw.porty;

import at.ac.hcw.porty.dto.ScanConfigDTO;
import at.ac.hcw.porty.listeners.PortScanUIListener;
import at.ac.hcw.porty.scanner.Scanner;
import at.ac.hcw.porty.scanner.ScannerFactory;
import at.ac.hcw.porty.types.Host;
import at.ac.hcw.porty.types.PortRange;
import at.ac.hcw.porty.types.ScanConfig;
import at.ac.hcw.porty.types.ScanStrategy;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.collections.ListChangeListener;

import java.time.Duration;
import java.util.Objects;

public class DashboardController {
    @FXML
    private Button startScanButton;
    @FXML
    private ListView<String> scanProgressConsole;
    @FXML
    private TextField ipTextField;

    ObservableList<String> consoleLines = FXCollections.observableArrayList();

    private boolean onScan = false;
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
    }

    @FXML
    protected void onScanStartButtonClick() throws InterruptedException {
        scanConfigDTO.setHost(ipTextField.getText());
        if(!onScan) {
            if(!scanConfigDTO.getHost().isEmpty() && scanConfigDTO.getHost()!=null) {
                startScanButton.setText("Stop");
                onScan = true;
                new Thread(() -> {
                    handle = scan(new Host(scanConfigDTO.getHost()), new PortRange(1, 50));
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
        ScanConfig config = new ScanConfig(host, range, Duration.ofMillis(10000));
        Scanner scanner = new Scanner(ScannerFactory.create(ScanStrategy.MOCK));

        ScanHandle handle = scanner.scan(config, new PortScanUIListener(consoleLines));

        return handle;
    }
}
