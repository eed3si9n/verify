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

package cutest.runner

import cutest.runner.Framework.ModuleFingerprint
import sbt.testing.{ Framework => BaseFramework, _ }

class Framework extends BaseFramework {
  def name(): String = "cutest"

  def options: Options = Options()

  def fingerprints(): Array[Fingerprint] =
    Array(ModuleFingerprint)

  def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader): Runner =
    new cutest.runner.Runner(args, remoteArgs, options, testClassLoader)

  def slaveRunner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader,
      send: String => Unit
  ): Runner =
    runner(args, remoteArgs, testClassLoader)
}

object Framework {

  /**
   * A fingerprint that searches only for singleton objects
   * of type [[cutest.api.AbstractTestSuite]].
   */
  object ModuleFingerprint extends SubclassFingerprint {
    val isModule = true
    def requireNoArgConstructor(): Boolean = true
    def superclassName(): String = "cutest.api.AbstractTestSuite"
  }
}
