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
    requires javafx.graphics;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.google.common;
    requires javafx.swing;
    requires org.apache.pdfbox;

    opens at.ac.hcw.porty.types.enums
            to com.fasterxml.jackson.databind,
            com.fasterxml.jackson.dataformat.xml;
    opens at.ac.hcw.porty.types.interfaces
            to com.fasterxml.jackson.databind,
            com.fasterxml.jackson.dataformat.xml;
    opens at.ac.hcw.porty.types.records
            to com.fasterxml.jackson.databind,
            com.fasterxml.jackson.dataformat.xml;
    opens at.ac.hcw.porty.types
            to com.fasterxml.jackson.databind,
            com.fasterxml.jackson.dataformat.xml;

    opens at.ac.hcw.porty to javafx.fxml;
    exports at.ac.hcw.porty;
    exports at.ac.hcw.porty.controller;
    opens at.ac.hcw.porty.controller to javafx.fxml;
    opens at.ac.hcw.porty.utils to com.fasterxml.jackson.databind, com.fasterxml.jackson.dataformat.xml;
}