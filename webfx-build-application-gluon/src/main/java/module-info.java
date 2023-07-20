// File managed by WebFX (DO NOT EDIT MANUALLY)

module webfx.build.application.gluon {

    // Direct dependencies modules
    //requires javafx.media;
    requires webfx.build.application;
    requires webfx.kit.openjfx;
    requires webfx.platform.audio.gluon;
    requires webfx.platform.boot.java;
    requires webfx.platform.console.java;
    requires webfx.platform.json.java;
    requires webfx.platform.resource.gluon;
    requires webfx.platform.scheduler.java;
    requires webfx.platform.shutdown.gluon;

    // Meta Resource package
    opens dev.webfx.platform.meta.exe;

}