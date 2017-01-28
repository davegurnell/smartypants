organization := "com.davegurnell"

name := "smartypants"

version := "0.1.0-SNAPSHOT"

scalaOrganization := "org.typelevel"
scalaVersion := "2.12.0"

crossScalaVersions := Seq("2.11.8", "2.12.0")

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:experimental.macros"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.scala-lang"   % "scala-reflect" % scalaVersion.value,
  "org.typelevel"   %% "macro-compat"  % "1.1.1",
  "org.scalatest"   %% "scalatest"     % "3.0.1" % "test",
  "com.chuusai"     %% "shapeless"     % "2.3.2" % "test"
)
