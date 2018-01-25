scalaVersion := "2.12.4"
organization := "org.jcryptool"
name := "bouncycryptool.plugin"
version := "0.1.1-SNAPSHOT"

retrieveManaged := true

// This plugin is for writing java code through the Eclipse IDE. The Scala-written connector has a
// bct.connector.java package with convenient Java-to-scala bindings.

libraryDependencies += "org.jcryptool" %% "bouncycryptool-connector" % bctplugin.Keys.bctVersion.value withSources()

// this plugin is not published via maven, it is part of the jcryptool core

publish := ()
publishLocal := ()

resolvers += Resolver.mavenLocal

// The plugin's speciality is to strip the maven-dependency
// to the target platform and to transform it into
// Eclipse PDE bundle dependencies as per
//  - project/EclipseTemplate.scala
excludeDependencies ++= Seq(
  SbtExclusionRule("org.jcryptool", "bouncycryptool-platform_2.12")
)

logLevel := Level.Error