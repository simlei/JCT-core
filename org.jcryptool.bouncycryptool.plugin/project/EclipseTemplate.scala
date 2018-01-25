import java.io.File
import sbt._

class EclipseTemplate (
                       val baseDir: File,
                       val templateDir: File,

                       val projectId: String,
                       val projectVersion: String,
                       val activatorClass: String,
                       val requiredBundles: Seq[String],
                       val classpathJars: Seq[(File, Option[File])],
                       val importPackageDeclarations: Seq[String] = Seq()

                     ) extends TemplateImplementations {
  val variables: Map[String, () => String] = Map(
    "project.id" -> (() => projectId),
    "project.version" -> `project.version` _,
    "manifest.requiredBundles" -> `manifest.requiredBundles`_,
    "manifest.importPackage" -> `manifest.importPackage` _,
    "manifest.activator" -> (() => activatorClass),
    "manifest.classpath" -> `manifest.classpath` _,
    "eclipsebuild.src" -> `eclipsebuild.src` _,
    "bin.includes" -> `bin.includes` _,
    "libraryClasspaths" -> libraryClasspaths _,

    "empty" -> (() => "")
  )

  def applyTemplate() = {
    val rebasePairs = ((templateDir ** "*").get.pair(rebase(templateDir, baseDir))) map { case (originF, destF) =>
      (originF, new File(destF.getParent, applyVariables(originF.getName)))
    }
    for((origin, dest) <- rebasePairs) {
      if(origin.isFile && !origin.name.endsWith("dontapply")) applyTemplateForSingleFile(origin, dest)
    }
  }

  def applyTemplateForSingleFile(origin: File, dest: File) = {
//    IO.createDirectory(dest)
    IO.write(dest, applyVariables(IO.read(origin)))
  }

  def applyVariables(origin: String) = {
    var replaced = origin
    for((literal, replacement) <- variables) {
      replaced = replaced.replaceAllLiterally("${"+literal+"}", replacement())
    }
    replaced
  }

  def relativize(f: File, base: File = baseDir) = sbt.IO.relativize(base, f)

}
object EclipseTemplate {
}

trait TemplateImplementations { self: EclipseTemplate =>

  def manifestList(header: String, items: Seq[String], escapeNewlines: Boolean = false): String = if(items.isEmpty) ""
  else if(escapeNewlines) {
    items.mkString(header, ",\\\n ", "")
  } else {
    items.mkString(header, ",\n ", "")
  }

  def libraryClasspaths = {
    def template(binJar: File, sourceJar: Option[File]) = {
      if(sourceJar.isDefined) {
        s"""<classpathentry sourcepath="./${relativize(sourceJar.get).get}" kind="lib" path="./${relativize(binJar).get}"/>"""
      } else {
        s"""<classpathentry kind="lib" path="./${relativize(binJar).get}"/>"""
      }
    }
    classpathJars.map(pair => template(pair._1, pair._2)).mkString("\n")
  }

  def `project.version` = projectVersion.replaceAll("-SNAPSHOT", "") + ".qualifier"

  def `manifest.requiredBundles` = manifestList("Require-Bundle: ", requiredBundles)
  def `manifest.importPackage` = manifestList("Import-Package: ", importPackageDeclarations)

  def `manifest.classpath` = manifestList("Bundle-ClassPath: .,\n ",
    classpathJars.map(pair => pair._1).map(sbt.IO.relativize(baseDir, _).getOrElse(sys.error("!!!error #1 in Eclipse PDE templating"))))
  def `eclipsebuild.src` = """src/"""
  def `bin.includes` = """plugin.xml,\
                         |               META-INF/,\
                         |               .,\
                         |               lib_managed/,\
                         |               icons/""".stripMargin
}

//object PDEFiles {
//  def changeClasspathfile(classpathfileContent: String) = {
//    val cpTag = "<classpath>"
//    val cpPDE =
//      "  <classpathentry kind=\"con\" path=\"org.eclipse.pde.core.requiredPlugins\"/>"
//    val buildersAdded = classpathfileContent.replaceAllLiterally(cpTag, cpTag + "\n" + cpPDE)
//    buildersAdded
//  }
//
//  def changeProjectfile(content: String) = {
//    val natureTag = "<nature>org.eclipse.jdt.core.javanature</nature>"
//    val addedTag = "<nature>org.eclipse.pde.PluginNature</nature>"
//    val natureAdded = content.replaceAllLiterally(natureTag, natureTag + "\n    " + addedTag)
//
//    val buildspecTag = "<buildSpec>"
//    val builders =
//      """    <buildCommand>
//        |      <name>org.eclipse.pde.ManifestBuilder</name>
//        |    </buildCommand>
//        |    <buildCommand>
//        |      <name>org.eclipse.pde.SchemaBuilder</name>
//        |    </buildCommand>""".stripMargin
//    val buildersAdded = natureAdded.replaceAllLiterally(buildspecTag, buildspecTag + "\n" + builders)
//
//    buildersAdded
//  }
//
//  def manifest(
//              pluginId: String,
//              version: String,
//              activator: String,
//              classpathJars: Seq[File],
//              baseDir: File
//              ) = {
//    s"""Manifest-Version: 1.0
//      |Bundle-ManifestVersion: 2
//      |Bundle-Name: BouncyCastle connector
//      |Bundle-SymbolicName: $pluginId;singleton:=true
//      |Bundle-Version: ${version.replaceAll("-SNAPSHOT", "")}.qualifier
//      |Bundle-RequiredExecutionEnvironment: JavaSE-1.8
//      |Import-Package: org.jcryptool.editor.hex,
//      | org.jcryptool.editor.hex.commands,
//      | org.jcryptool.editor.hex.service
//      |Require-Bundle: org.eclipse.ui,
//      | org.eclipse.core.runtime,
//      | org.jcryptool.core.operations,
//      | org.jcryptool.core.logging,
//      | org.jcryptool.core.util,
//      | org.eclipse.ui.workbench,
//      | org.jcryptool.editor.text
//      |Bundle-ActivationPolicy: lazy
//      |Bundle-Activator: $activator
//      |Bundle-ClassPath: """.stripMargin + classpathJars.map(sbt.IO.relativize(baseDir, _).getOrElse("!!!error #1 in PDEFiles.scala")).mkString("", ",\\\n ", "\n")
//  }
//
//  def buildproperties() = {
//    """source.. = src/main/scala/,\
//      |           src/main/java/
//      |output.. = bin/
//      |bin.includes = plugin.xml,\
//      |               META-INF/,\
//      |               .,\
//      |               lib_managed/,\
//      |               icons/
//      |jars.compile.order = .""".stripMargin
//  }
//}

/*

 */
