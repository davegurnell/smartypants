organization := "com.davegurnell"
name         := "smartypants"
version      := "0.1.0"
licenses     += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

scalaOrganization  := "org.typelevel"
scalaVersion       := "2.12.0"
crossScalaVersions := Seq("2.11.8", "2.12.0")

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:experimental.macros"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "org.scala-lang"   % "scala-reflect" % scalaVersion.value,
  "org.typelevel"   %% "macro-compat"  % "1.1.1",
  "org.scalatest"   %% "scalatest"     % "3.0.1" % "test",
  "com.chuusai"     %% "shapeless"     % "2.3.2" % "test"
)

pomExtra in Global := {
  <url>https://github.com/davegurnell/smartypants</url>
  <scm>
    <connection>scm:git:github.com/davegurnell/smartypants</connection>
    <developerConnection>scm:git:git@github.com:davegurnell/smartypants</developerConnection>
    <url>github.com/davegurnell/smartypants</url>
  </scm>
  <developers>
    <developer>
      <id>davegurnell</id>
      <name>Dave Gurnell</name>
      <url>http://davegurnell.com</url>
      <organization>Underscore</organization>
      <organizationUrl>http://underscore.io</organizationUrl>
    </developer>
  </developers>
}
