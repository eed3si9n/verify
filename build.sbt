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
// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{ crossProject, CrossType }
import sbt.Keys._
import com.typesafe.sbt.GitVersioning

addCommandAlias("ci-all", ";+clean ;+test:compile ;+test ;+package")
addCommandAlias("release", ";+clean ;+minitestNative/clean ;+publishSigned ;+minitestNative/publishSigned")

val Scala211 = "2.11.12"
val Scala212 = "2.12.9"
val Scala213 = "2.13.0"
val Scala3 = "0.17.0-RC1"

ThisBuild / scalaVersion := "2.12.8"
ThisBuild / crossScalaVersions := Seq(Scala211, Scala212, Scala213)

ThisBuild / organization := "com.eed3si9n.cutest"
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

def scalaPartV = Def setting (CrossVersion partialVersion scalaVersion.value)
lazy val crossVersionSharedSources: Seq[Setting[_]] =
  (Seq(Compile, Test).map { sc =>
    (unmanagedSourceDirectories in sc) ++= {
      (unmanagedSourceDirectories in sc).value.map { dir =>
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
  scalacOptions in ThisBuild ++= Seq(
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
    case Some((2, v)) if v >= 12 =>
      scalaLinterOptions
    case Some((2, 12)) =>
      scalaLinterOptions ++ scalaTwoTwelveDeprecatedOptions
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
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
    Resolver.sonatypeRepo("releases")
  ),
  testFrameworks := Seq(new TestFramework("cutest.runner.Framework")),
  headerLicense := (ThisBuild / headerLicense).value
)

lazy val scalaJSSettings = Seq(
  scalaJSStage in Test := FastOptStage
)

lazy val nativeSettings = Seq(
  scalaVersion := Scala211,
  crossScalaVersions := Seq(Scala211),
  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
)

lazy val needsScalaParadise = settingKey[Boolean]("Needs Scala Paradise")

lazy val requiredMacroCompatDeps = Seq(
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

lazy val minitestRoot = project
  .in(file("."))
  .aggregate(minitestJVM, minitestJS)
  .settings(
    name := "minitest root",
    Compile / sources := Nil,
    skip in publish := true
  )

lazy val minitest = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("."))
  .settings(
    name := "minitest",
    sharedSettings,
    crossVersionSharedSources,
    requiredMacroCompatDeps
  )
  .jvmSettings(
    crossScalaVersions += Scala3,
    libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0"
  )
  .platformsSettings(NativePlatform)(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % Provided
  )
  .jsSettings(
    scalaJSSettings,
    libraryDependencies ++= Seq(
      "org.portable-scala" %%% "portable-scala-reflect" % "0.1.0",
      "org.scala-js" %% "scalajs-test-interface" % scalaJSVersion
    )
  )
  .nativeSettings(
    nativeSettings,
    libraryDependencies += "org.scala-native" %%% "test-interface" % nativeVersion
  )

lazy val minitestJVM = minitest.jvm
lazy val minitestJS = minitest.js
lazy val minitestNative = minitest.native
