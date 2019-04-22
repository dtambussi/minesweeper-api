name := """minesweeper-api"""

organization := "com.dt"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  guice,
  jdbc,
  evolutions,
  "mysql" % "mysql-connector-java" % "8.0.15",
  "org.playframework.anorm" %% "anorm" % "2.6.2"
)