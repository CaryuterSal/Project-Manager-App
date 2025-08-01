open module dev.builder {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires jakarta.validation;
    requires org.jetbrains.annotations;
    requires org.apache.tika.core;
    requires com.healthmarketscience.jackcess;
    requires java.xml.crypto;
    requires org.apache.commons.collections4;
    requires com.fasterxml.jackson.databind;
    requires org.hibernate.validator;
    requires org.reflections;
    requires ucp;
    requires org.apache.poi.poi;
    requires org.slf4j;
    requires jdk.unsupported;
    requires ojdbc8;
    requires password4j;

    exports dev.builder;
}
