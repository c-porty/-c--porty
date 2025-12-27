package at.ac.hcw.porty;

import at.ac.hcw.porty.listeners.PortScanUIListener;
import at.ac.hcw.porty.scanner.Scanner;
import at.ac.hcw.porty.scanner.ScannerFactory;
import at.ac.hcw.porty.types.Host;
import at.ac.hcw.porty.types.PortRange;
import at.ac.hcw.porty.types.ScanConfig;
import at.ac.hcw.porty.types.ScanStrategy;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.time.Duration;

public class DashboardController {
    @FXML
    private Button startScanButton;
    @FXML
    private TextArea scanProgressConsole;

    private boolean onScan = false;
    private ScanHandle handle;

    @FXML
    protected void onScanStartButtonClick() throws InterruptedException {
        if(!onScan) {
            startScanButton.setText("Stop");
            onScan = true;
            new Thread(()->{
                handle = scan(new Host("localhost"), new PortRange(1, 50));
                handle.summary().join();
                Platform.runLater(() -> {
                    startScanButton.setText("Start Scan");
                    onScan=false;
                });
            }).start();

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

        ScanHandle handle = scanner.scan(config, new PortScanUIListener(scanProgressConsole));

        return handle;
    }
}
