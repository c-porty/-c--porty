package at.ac.hcw.porty.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class CreditsController {
    @FXML
    private ImageView creditsImage;

    @FXML
    private TextArea creditsTextArea;

    @FXML
    public void initialize() throws IOException {
        Image image = new Image(
                getClass().getResource("/at/ac/hcw/porty/images/credits-test-foto.png").toExternalForm());

        creditsImage.setImage(image);

        String text = loadText("/at/ac/hcw/porty/texts/credits-text.txt");
        creditsTextArea.setText(text);
    }

    private String loadText(String path) throws IOException {
        try (InputStream text = getClass().getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(text, StandardCharsets.UTF_8))) {
            //InputStreamReader wandelt InputStream von Bytes in Chars um

            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
