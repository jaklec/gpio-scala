import sbt._
import Keys._

object GpioScalaBuild extends Build {

  override val settings = super.settings ++ Seq(
    name := "gpio-scala",
    organization := "se.jaklec",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.10.3",
    scalacOptions := Seq("-feature", "-deprecation"),
    javacOptions := Seq("-source", "1.7", "-target", "1.7")
  )
  
  lazy val scalaTestVersion = "2.0"
  lazy val scalaTestLibs = Seq("org.scalatest" %% "scalatest" % scalaTestVersion % "test")

  lazy val gpioScala = Project("gpio-scala", file("."), settings = Project.defaultSettings
  ).settings(
    libraryDependencies ++= scalaTestLibs
  )
}
