// File managed by WebFX (DO NOT EDIT MANUALLY)

module webfx.build.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media;
    requires webfx.platform.json;
    requires webfx.platform.resource;
    requires webfx.platform.scheduler;
    requires webfx.platform.util;

    // Exported packages
    exports com.orangomango.retoohs;
    exports com.orangomango.retoohs.game;
    exports com.orangomango.retoohs.ui;

    // Resources packages
    opens audio;
    opens files;
    opens images;

    // Provided services
    provides javafx.application.Application with com.orangomango.retoohs.MainApplication;

}