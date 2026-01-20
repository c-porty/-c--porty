package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.types.enums.Language;
import at.ac.hcw.porty.utils.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CreditsController {
    private static final Logger logger =
            LoggerFactory.getLogger(CreditsController.class);

    @FXML private ImageView creditsImage;
    @FXML private TextArea creditsTextArea;
    @FXML private Label aboutUsLabel;
    @FXML private Label cSharpLabel;

    @FXML
    public void initialize() {
        Image image = new Image(
                getClass().getResource("/at/ac/hcw/porty/images/team-photo.png").toExternalForm()
        );
        creditsImage.setImage(image);

        setupLanguageTexts();

        //Change text on language switch
        loadCreditsTextForCurrentLanguage();
        I18n.languageProperty().addListener(
                (obs,
                 oldLang,
                 newLang) ->
                        loadCreditsTextForCurrentLanguage()
        );

        creditsTextArea.setWrapText(true);
        creditsTextArea.setEditable(false);
    }

    private void setupLanguageTexts() {
        aboutUsLabel.textProperty().bind(I18n.bind("about-us"));
        cSharpLabel.textProperty().bind(I18n.bind("credits.cSharp"));
    }

    private void loadCreditsTextForCurrentLanguage() {
        String path = resolveCreditsTextPath(I18n.getLanguage());
        String text = readResourceText(path);
        if (text == null) {
            //Path to image
            text = readResourceText("/at/ac/hcw/porty/texts/credits-text-en.txt");
            if (text == null) {
                text = I18n.bind("credits.text-missing").get();
            }
        }
        creditsTextArea.setText(text);
    }

    private String resolveCreditsTextPath(Language lang) {
        return switch (lang) {
            case DE -> "/at/ac/hcw/porty/texts/credits-text-de.txt";
            case EN -> "/at/ac/hcw/porty/texts/credits-text-en.txt";
        };
    }

    /* try to get and load file */
    private String readResourceText(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) return null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!sb.isEmpty()) sb.append('\n');
                    sb.append(line);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            logger.error("Error reading resource text: {}", path, e);
            return null;
        }
    }
}