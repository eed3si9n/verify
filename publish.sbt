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

import scala.xml.Elem
import scala.xml.transform.{ RewriteRule, RuleTransformer }

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/monix/minitest"),
    "scm:git@github.com:monix/minitest.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "alexelcu",
    name = "Alexandru Nedelcu",
    email = "noreply@alexn.org",
    url = url("https://alexn.org")
  )
)

// -- Settings meant for deployment on oss.sonatype.org
ThisBuild / sonatypeProfileName := (ThisBuild / organization).value
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
ThisBuild / pomIncludeRepository := { _ =>
  false
} // removes optional dependencies

// For evicting Scoverage out of the generated POM
// See: https://github.com/scoverage/sbt-scoverage/issues/153
ThisBuild / pomPostProcess := { (node: xml.Node) =>
  new RuleTransformer(new RewriteRule {
    override def transform(node: xml.Node): Seq[xml.Node] = node match {
      case e: Elem
          if e.label == "dependency" && e.child
            .exists(child => child.label == "groupId" && child.text == "org.scoverage") =>
        Nil
      case _ => Seq(node)
    }
  }).transform(node).head
}

enablePlugins(GitVersioning)

/* The BaseVersion setting represents the in-development (upcoming) version,
 * as an alternative to SNAPSHOTS.
 */
git.baseVersion := "2.5.0"

val ReleaseTag = """^v(\d+\.\d+\.\d+(?:[-.]\w+)?)$""".r
git.gitTagToVersionNumber := {
  case ReleaseTag(v) => Some(v)
  case _             => None
}

git.formattedShaVersion := {
  val suffix = git.makeUncommittedSignifierSuffix(git.gitUncommittedChanges.value, git.uncommittedSignifier.value)

  git.gitHeadCommit.value map { _.substring(0, 7) } map { sha =>
    git.baseVersion.value + "-" + sha + suffix
  }
}
