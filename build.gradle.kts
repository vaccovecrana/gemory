plugins {
  id("io.vacco.oss.gitflow") version "1.11.0"
  id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "io.vacco.gemory"
version = "0.7.0"

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  orgConfigUrl("https://raw.githubusercontent.com/vaccovecrana/org-config/refs/heads/main/vacco-oss-java-25.json")
  addJ8Spec()
  addClasspathHell()
  sharedLibrary(true, false)
}

dependencies {
  testImplementation("io.vacco.shax:shax:2.0.17.2")
}

configure<io.vacco.cphell.ChPluginExtension> {
  resourceExclusions.addAll(listOf("module-info.class"))
}

javafx {
  version = "27"
  modules("javafx.controls")
}

tasks.test {
  systemProperty("gemory.test.desktop", System.getProperty("gemory.test.desktop", "true"))
}

tasks.register<JavaExec>("runDemo") {
  group = "application"
  description = "Runs the manual Gemory demo"
  dependsOn(tasks.testClasses)

  val testRuntime = sourceSets["test"].runtimeClasspath
  val javaFxJars = testRuntime.filter { it.name.startsWith("javafx-") && it.name.endsWith(".jar") }
  classpath = testRuntime.filter { !(it.name.startsWith("javafx-") && it.name.endsWith(".jar")) }
  mainClass.set("io.vacco.gemory.GmSpecShell")
  jvmArgs(
      "--enable-native-access=javafx.graphics",
      "--module-path", javaFxJars.asPath,
      "--add-modules", "javafx.controls"
  )
}