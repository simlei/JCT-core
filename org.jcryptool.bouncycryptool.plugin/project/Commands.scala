package bctplugin

import sbt._
import sbt.Keys._
import scala.util.matching._


object Commands {

  object simpleRE {
    import scala.util.Try

    object ToInt {
      def unapply(in: String): Option[Int] = Try(in.toInt).toOption
    }

    implicit class RegexHelper(val sc: StringContext) extends AnyVal {
      def re: scala.util.matching.Regex =
        sc.parts
          .map(java.util.regex.Pattern.quote)
          .reduce(_ + "(.*)" + _)
          .mkString
          .r
    }
  }

  def writeBCTVersionToFile(dependencyVersionFile: File, version: String) = {
    import simpleRE._

    val newContent = IO.read(dependencyVersionFile).linesIterator.map{ line =>
      line match {
        case re"""${start}.bctVersion := "${oldversion}"""" => s"""${start}.bctVersion := "$version""""
        case omega => omega
      }
    }

    IO.write(dependencyVersionFile, newContent.mkString("\n"))
  }

  def updatePluginCmd: Command = Command.single("updatePlugin"){ case (state, arg) =>
    val extracted = Project.extract(state)
    println(s"-- Setting the BCT dependency to $arg")
    writeBCTVersionToFile(extracted.get(baseDirectory) / "bctVersion.sbt", arg)
    extracted.append(Seq(
      bctplugin.Keys.bctVersion := arg
    ), state)
      .copy(remainingCommands = "fetchBCT" +: state.remainingCommands)
  }

}