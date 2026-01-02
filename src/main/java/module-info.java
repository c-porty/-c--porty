module at.ac.hcw.porty {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.desktop;
    requires org.slf4j;
    requires javafx.base;

    opens at.ac.hcw.porty to javafx.fxml;
    exports at.ac.hcw.porty;
    exports at.ac.hcw.porty.controller;
    opens at.ac.hcw.porty.controller to javafx.fxml;
}