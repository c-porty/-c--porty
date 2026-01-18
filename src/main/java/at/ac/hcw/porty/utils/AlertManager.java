package at.ac.hcw.porty.utils;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import java.util.ArrayList;
import java.util.List;

public class AlertManager {
    public static Alert createAlert(
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
                .add(AlertManager.class.getResource("/at/ac/hcw/porty/styles/styles_dark.css").toExternalForm());

        return alert;
    }
}
