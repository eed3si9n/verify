/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
// shadow sbt-scalajs' crossProject and CrossType until we drop Scala.js 0.6 from the crossbuild
import sbtcrossproject.{ crossProject, CrossType }

addCommandAlias("ci-all", ";+clean ;+test:compile ;+test ;+package")
addCommandAlias("release", ";+clean ;+verifyNative/clean ;+publishSigned ;+verifyNative/publishSigned")

val Scala211 = "2.11.12"
val Scala212 = "2.12.15"
val Scala213 = "2.13.8"
val Scala3 = "3.0.2"
val Scala31 = "3.1.3"

ThisBuild / scalaVersion := Scala212

ThisBuild / organization := "com.eed3si9n.verify"
ThisBuild / homepage := Some(url("https://www.scala-lang.org"))
ThisBuild / startYear := Some(2002)
ThisBuild / licenses += (("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))
ThisBuild / headerLicense := Some(
  HeaderLicense.Custom(
    s"""Scala (${(ThisBuild / homepage).value.get})
     |
     |Copyright EPFL and Lightbend, Inc.
     |
     |Licensed under Apache License 2.0
     |(http://www.apache.org/licenses/LICENSE-2.0).
     |
     |See the NOTICE file distributed with this work for
     |additional information regarding copyright ownership.
     |""".stripMargin
  )
)

val ReleaseTag = """^v(\d+\.\d+\.\d+(?:[-.]\w+)?)$""".r

lazy val verifyRoot = (project in file("."))
  .aggregate(verifyJVM, verifyJS, verifyNative)
  .settings(
    name := "verify root",
    Compile / sources := Nil,
    publish / skip := true,
    crossScalaVersions := Nil
  )

lazy val verify = (crossProject(JVMPlatform, JSPlatform, NativePlatform) in file("."))
  .settings(
    name := "verify",
    sharedSettings,
    crossVersionSharedSources,
    libraryDependencies ++= {
      val crossVer = CrossVersion.partialVersion(scalaVersion.value)
      crossVer match {
        case Some((2, _)) =>
          List(
            "org.scala-lang" % "scala-reflect" % scalaVersion.value % Compile,
            "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided
          )
        case _ => Nil
      }
    }
  )
  .jvmSettings(
    crossScalaVersions := Seq(Scala211, Scala212, Scala213, Scala3),
    libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0"
  )
  .jsSettings(
    crossScalaVersions := Seq(Scala211, Scala212, Scala213, Scala3),
    libraryDependencies ++= {
      val sv = scalaBinaryVersion.value
      if (sv.startsWith("3"))
        // https://github.com/portable-scala/portable-scala-reflect/issues/23
        Seq(
          "org.portable-scala" % "portable-scala-reflect_sjs1_2.13" % "1.1.2",
          "org.scala-js" % "scalajs-test-interface_2.13" % scalaJSVersion
        )
      else
        Seq(
          "org.portable-scala" %%% "portable-scala-reflect" % "1.1.2",
          "org.scala-js" %% "scalajs-test-interface" % scalaJSVersion
        )
    },
    Test / scalaJSStage := FastOptStage
  )
  .nativeSettings(
    libraryDependencies ++= Seq(
      "org.scala-native" %%% "test-interface" % nativeVersion
    ),
    libraryDependencies ++= {
      if (scalaBinaryVersion.value != "3") {
        Seq(
          compilerPlugin(
            "com.github.ghik" % "silencer-plugin" % "1.7.8" cross CrossVersion.full
          )
        )
      } else {
        Nil
      }
    },
    libraryDependencies += {
      if (scalaBinaryVersion.value == "3") {
        "com.github.ghik" % "silencer-lib_2.13.7" % "1.7.8" % Provided
      } else {
        "com.github.ghik" % "silencer-lib" % "1.7.8" % Provided cross CrossVersion.full // required for 0.3.9 support
      }
    },
    nativeLinkStubs := true, // required for 0.3.9 support
    scalaVersion := Scala211,
    crossScalaVersions := Seq(Scala211, Scala212, Scala213, Scala31),
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
  )

lazy val verifyJVM = verify.jvm
lazy val verifyJS = verify.js
lazy val verifyNative = verify.native

def scalaPartV = Def.setting(CrossVersion partialVersion scalaVersion.value)
lazy val crossVersionSharedSources: Seq[Setting[_]] =
  (Seq(Compile, Test).map { sc =>
    (sc / unmanagedSourceDirectories) ++= {
      (sc / unmanagedSourceDirectories).value.map { dir =>
        scalaPartV.value match {
          case Some((major, minor)) =>
            new File(dir.getPath + s"_$major.$minor")
          case None =>
            throw new NoSuchElementException("Scala version")
        }
      }
    }
  }) ++ Seq(
    Compile / unmanagedSourceDirectories += {
      val crossVer = CrossVersion.partialVersion(scalaVersion.value)
      crossVer match {
        case Some((2, _)) => baseDirectory.value.getParentFile / "shared" / "src" / "main" / "scala-2"
        case _            => baseDirectory.value.getParentFile / "shared" / "src" / "main" / "scala-3"
      }
    }
  )

lazy val scalaLinterOptions =
  Seq(
    // Enables linter options
    "-Xlint:adapted-args", // warn if an argument list is modified to match the receiver
    "-Xlint:nullary-unit", // warn when nullary methods return Unit
    "-Xlint:inaccessible", // warn about inaccessible types in method signatures
    "-Xlint:nullary-override", // warn when non-nullary `def f()' overrides nullary `def f'
    "-Xlint:infer-any", // warn when a type argument is inferred to be `Any`
    "-Xlint:missing-interpolator", // a string literal appears to be missing an interpolator id
    "-Xlint:doc-detached", // a ScalaDoc comment appears to be detached from its element
    "-Xlint:private-shadow", // a private field (or class parameter) shadows a superclass field
    "-Xlint:type-parameter-shadow", // a local type parameter shadows a type already in scope
    "-Xlint:poly-implicit-overload", // parameterized overloaded implicit methods are not visible as view bounds
    "-Xlint:option-implicit", // Option.apply used implicit view
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit
    "-Xlint:package-object-classes" // Class or object defined in package object
  )

lazy val scalaTwoTwelveDeprecatedOptions =
  Seq(
    // Deprecated in 2.12, removed in 2.13
    "-Ywarn-inaccessible",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit"
  )

lazy val sharedSettings = Seq(
  ThisBuild / scalacOptions ++= Seq(
    // Note, this is used by the doc-source-url feature to determine the
    // relative path of a given source file. If it's not a prefix of a the
    // absolute path of the source file, the absolute path of that file
    // will be put into the FILE_SOURCE variable, which is
    // definitely not what we want.
    "-sourcepath",
    file(".").getAbsolutePath.replaceAll("[.]$", "")
  ),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Xlint",
    "-Ywarn-dead-code",
    "-Xlog-free-terms"
  ),
  // Version specific options
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v > 12 =>
      scalaLinterOptions ++ Seq("-Wunused:-implicits", "-Xfatal-warnings")
    case Some((2, 12)) =>
      scalaLinterOptions ++ scalaTwoTwelveDeprecatedOptions ++ Seq("-Ywarn-unused:-implicits", "-Xfatal-warnings")
    case Some((2, 11)) =>
      scalaLinterOptions ++ Seq("-target:jvm-1.8") ++ scalaTwoTwelveDeprecatedOptions
    case _ =>
      Seq("-target:jvm-1.8")
  }),
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11 | 12)) =>
      Seq(
        "-Xlint:unsound-match", // Pattern match may not be typesafe
        "-Xlint:by-name-right-associative", // By-name parameter of right associative operator
        "-Ywarn-adapted-args"
      )
    case _ =>
      Nil
  }),
  resolvers ++= Seq(
    "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases"
  ),
  testFrameworks := Seq(new TestFramework("verify.runner.Framework")),
  headerLicense := (ThisBuild / headerLicense).value
)

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/scala/nanotest-strawman"),
    "scm:git@github.com:scala/nanotest-strawman.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "alexelcu",
    name = "Alexandru Nedelcu",
    email = "noreply@alexn.org",
    url = url("https://alexn.org")
  ),
  Developer(
    id = "eed3i9n",
    name = "Eugene Yokota",
    email = "@eed3si9n",
    url = url("https://eed3si9n.com")
  )
)

// -- Settings meant for deployment on oss.sonatype.org
ThisBuild / publishMavenStyle := true
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / isSnapshot := {
  (ThisBuild / version).value endsWith "SNAPSHOT"
}
ThisBuild / Test / publishArtifact := false
ThisBuild / pomIncludeRepository := { _ => false } // removes optional dependencies
