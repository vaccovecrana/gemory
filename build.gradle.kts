plugins {
  id("io.vacco.oss.gitflow") version "0.9.8"
  id("org.openjfx.javafxplugin") version "0.0.10"
}

group = "io.vacco.gemory"
version = "0.3.0"

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  addJ8Spec()
  addClasspathHell()
  sharedLibrary(true, false)
}

dependencies {
  implementation("org.slf4j:slf4j-api:1.7.30")
  testImplementation("io.vacco.shax:shax:1.7.30.0.0.7")
}

configure<io.vacco.cphell.ChPluginExtension> {
  resourceExclusions.addAll(listOf("module-info.class"))
}

javafx {
  version = "16"
  modules("javafx.controls")
}
