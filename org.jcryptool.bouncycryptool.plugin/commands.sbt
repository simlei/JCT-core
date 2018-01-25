addCommandAlias("fetchBCT", "; clean; update; applyEclipseTemplate")
commands += bctplugin.Commands.updatePluginCmd // "updatePlugin <bctVersion>"

/// -- eclipse stuff

val templateDirectory = settingKey[File]("the directory with template files")
val managedJarsDirectory = settingKey[File]("directory where the sbt-managed libraries get resolved into")
val eclipseProjectId = settingKey[String]("the eclipse project id")
val pdeActivatorClass = settingKey[String]("the eclipse plugin activator class extending an eclipse plugin class e.g. AbstractUIPlugin")
val binSourceClasspath = taskKey[Seq[(File, Option[File])]]("pair of binary jar files and optional sources to be included in the classpath")
val pdeImportedPackagesList = settingKey[Seq[String]]("imported packages for the manifest file (so eclipse can work out P2 dependencies)")
val pdeRequiredBundlesList = settingKey[Seq[String]]("required bundles for the manifest file (so eclipse can work out P2 dependencies)")

val applyEclipseTemplate = taskKey[Unit]("writes custom eclipse files")


templateDirectory := baseDirectory.value / "eclipse_template"
managedJarsDirectory := baseDirectory.value / "lib_managed"
eclipseProjectId := organization.value + "." + name.value
pdeActivatorClass := "scalabc.connector.app.BouncyCrypToolPlugin"
binSourceClasspath := {
  val managedDir = managedJarsDirectory.value
  val jars = ( managedDir ** "*.jar") --- ( managedDir ** "srcs" ** "*.jar")
  jars.get.map(jar => (jar, None))
}

pdeImportedPackagesList := Seq()

pdeRequiredBundlesList := Seq(
  "org.eclipse.ui",
  "org.eclipse.ui.workbench",
  "org.eclipse.core.runtime",
  "org.jcryptool.core.operations",
  "org.eclipse.swt",
  "org.eclipse.jface",
  "org.jcryptool.core.logging",
  "org.jcryptool.core.util",
  "org.jcryptool.editor.text"
)

applyEclipseTemplate := {
  val baseDir: File = baseDirectory.value
  val templateDir: File = templateDirectory.value
  val libManagedDir: File = managedJarsDirectory.value

  val projectId: String = "org.jcryptool.bouncycryptool.plugin"
  val projectVersion: String = version.value
  val activatorClass: String = pdeActivatorClass.value
  val requiredBundles: Seq[String] = pdeRequiredBundlesList.value
  val classpathJars: Seq[(File, Option[File])] = binSourceClasspath.value //TODO: needs work with source jars, not included yet
  val importPackageDeclarations: Seq[String] = pdeImportedPackagesList.value

  val templateEngine = new EclipseTemplate(
    baseDir = baseDir,
    templateDir = templateDir,
    projectId = projectId,
    projectVersion = projectVersion,
    activatorClass = activatorClass,
    requiredBundles = requiredBundles,
    classpathJars = classpathJars,
    importPackageDeclarations = importPackageDeclarations
  )

  println(s"-- Updating the Eclipse project definition for project $projectId")
  templateEngine.applyTemplate()
}