package at.ac.hcw.porty.utils;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import java.util.ArrayList;
import java.util.List;

public class AlertManager {
    public static Alert createGridAlert(
            Alert.AlertType type,
            String title,
            String header,
            List<String> labels,
            int prefWidth,
            int prefHeight
    ) {
        if (labels.size() % 2 != 0) {
           throw new IllegalArgumentException("labels size must be even");
        }

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        grid.setPadding(new Insets(10));

        List<Label> nodes = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            Label label = new Label(labels.get(i));
            label.getStyleClass().add(i % 2 == 0 ? "alert-title" : "alert-text");
            nodes.add(label);
        }

        int iterator = 0;
        for (int i = 0; i < labels.size() / 2; i++) {
            grid.addRow(i, nodes.get(i + iterator++), nodes.get(i + iterator));
        }

        alert.getDialogPane().setContent(grid);
        alert.getDialogPane().setPrefSize(prefWidth, prefHeight);
        alert.getDialogPane().getStylesheets()
                .add(AlertManager.class.getResource("/at/ac/hcw/porty/styles/alerts/result.css").toExternalForm());

        return alert;
    }

    public static Alert createDangerAlert(String text, String confirmButtonText){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle(I18n.bind("alert.confirm-title").get());
        alert.setHeaderText(text);

        ButtonType deleteButton = new ButtonType(confirmButtonText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(I18n.bind("alert.cancel").get(), ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(deleteButton, cancelButton);
        alert.getDialogPane().getStylesheets()
                .add(AlertManager.class.getResource("/at/ac/hcw/porty/styles/alerts/delete.css").toExternalForm());
        return alert;
    }

    public static Alert createInfoAlert(String text){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(I18n.bind("alert.info").get());
        alert.setHeaderText(text);

        alert.getDialogPane().getStylesheets()
                .add(AlertManager.class.getResource("/at/ac/hcw/porty/styles/alerts/info.css").toExternalForm());

        return alert;
    }

    public static Alert createErrorAlert(String text){
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle(I18n.bind("alert.error").get());
        alert.setHeaderText(text);

        alert.getDialogPane().getStylesheets()
                .add(AlertManager.class.getResource("/at/ac/hcw/porty/styles/alerts/error.css").toExternalForm());

        return alert;
    }
}
