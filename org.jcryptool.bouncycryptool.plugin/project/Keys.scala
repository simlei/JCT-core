package bctplugin

import sbt._

object Keys {
  val bctVersion = settingKey[String]("Version of the BCT core to depend on")
}