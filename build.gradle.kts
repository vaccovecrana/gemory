plugins {
  id("io.vacco.oss.gitflow") version "0.9.7"
  id("org.openjfx.javafxplugin") version "0.0.9"
}

group = "io.vacco.gemory"
version = "0.1.0"

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  addJ8Spec()
  addClasspathHell()
  sharedLibrary(true, false)
}

dependencies {
  testImplementation("io.vacco.jsonbeans:jsonbeans:1.0.0")
}

configure<io.vacco.cphell.ChPluginExtension> {
  resourceExclusions.addAll(listOf("module-info.class"))
}

javafx {
  version = "16"
  modules("javafx.controls", "javafx.fxml") // TODO remove after initial dev done
}
