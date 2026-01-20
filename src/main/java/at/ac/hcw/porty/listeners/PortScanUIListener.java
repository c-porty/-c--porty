package at.ac.hcw.porty.listeners;

import at.ac.hcw.porty.controller.DashboardController;
import at.ac.hcw.porty.controller.MainController;
import at.ac.hcw.porty.types.enums.PortStatus;
import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.utils.AlertManager;
import at.ac.hcw.porty.utils.I18n;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortScanUIListener implements PortScanListener {
    private final ObservableList<String> outputTextList;
    private final MainController mainController;
    private final DashboardController dashboardController;

    private int currentStep = 1;

    public PortScanUIListener(ObservableList<String> list, MainController mainController, DashboardController dashboardController) {
        this.outputTextList = list;
        this.mainController = mainController;
        this.dashboardController = dashboardController;
    }

    @Override public void onStarted(ScanConfig config) {
        Platform.runLater(() -> {
            setProgressStep(2);
            outputTextList.clear();
            outputTextList.add(I18n.bind("listener.started").get() + " " + config.host().address());
        });
    }

    @Override public void onResult(PortScanResult result) {
        Platform.runLater(() -> {
            setProgressStep(4);
            outputTextList.add(I18n.bind("listener.result").get() + result.host().address() + ":" + result.port() + " -> " + result.status());
        });
    }

    @Override public void onComplete(ScanSummary summary) {
        Platform.runLater(() -> {
            setProgressStep(5);
            outputTextList.add(I18n.bind("listener.completed").get() + " " + summary.results().size() + " Ports");
            outputTextList.add(String.format("%s %s: ", I18n.bind("listener.detailed-information").get(), summary.host().address()));


            Alert alert = AlertManager.createGridAlert(
                    Alert.AlertType.CONFIRMATION,
                    I18n.bind("listener.scan-successful").get(),
                    I18n.bind("listener.short-summary").get(),
                    List.of(
                        I18n.bind("history.scanned-address").get(),
                        summary.host().address(),
                        I18n.bind("history.open-ports").get(),
                        String.valueOf(summary.results().size()),
                        I18n.bind("result.time-taken").get(),
                        String.format("%d s", summary.finishedAt().getEpochSecond()
                            - summary.startedAt().getEpochSecond())
                    ),
                    500,
                    200
            );
            ButtonType moreButton = new ButtonType(I18n.bind("button.more").get(), ButtonBar.ButtonData.OK_DONE);
            ButtonType closeButton = new ButtonType(I18n.bind("button.close").get(), ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(moreButton, closeButton);

            dashboardController.celebrateSuccess();

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == moreButton) {
                mainController.navigateToResults(summary);
            }
        });
    }

    @Override public void onError(Throwable t) {
        Platform.runLater(() -> {
            setProgressStep(0);
            outputTextList.add(I18n.bind("listener.error").get() + " " + t);
        });
    }

    @Override public void onProgress(String msg) {
        Platform.runLater(() -> {
            setProgressStep(3);
            outputTextList.add(I18n.bind("listener.progress").get() + " " + msg);

            Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)%");
            Matcher matcher = pattern.matcher(msg);

            if ((msg.contains("Connect Scan Timing") || msg.contains("SYN Stealth Scan Timing") || msg.contains("ARP Ping Scan Timing"))&& matcher.find()) {
                double percent = Double.parseDouble(matcher.group(1));
                dashboardController.setProgress(percent);
            }

            if(msg.contains("Nmap done")){
                dashboardController.setProgress(100.0);
            }
        });
    }

    @Override public void onCancel() {
        Platform.runLater(() -> {
            setProgressStep(0);
            outputTextList.add(I18n.bind("listener.cancelled").get());
        });
    }

    private void setProgressStep(int step){
        if(step != currentStep){
            System.out.println("Trying to set step: " + step + " current step: " + currentStep);
            dashboardController.setProgressStep(step);
            currentStep = step;
        }
    }
}
