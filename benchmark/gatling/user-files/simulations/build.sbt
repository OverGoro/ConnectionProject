import io.gatling.core.Predef._
import io.gatling.http.Predef._

enablePlugins(GatlingPlugin)

scalaVersion := "2.13.12"

val gatlingVersion = "3.9.5"

// Основные зависимости Gatling
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test,it"
libraryDependencies += "io.gatling" % "gatling-test-framework" % gatlingVersion % "test,it"