module at.ac.hcw.porty {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens at.ac.hcw.porty to javafx.fxml;
    exports at.ac.hcw.porty;
}