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

import sbt.testing.Logger

final class ConsoleLogger extends Logger {
  private[this] val withColors =
    System.getenv().get("TERM") != null

  def ansiCodesSupported(): Boolean =
    withColors
  def error(msg: String): Unit =
    print(msg)
  def warn(msg: String): Unit =
    print(msg)
  def info(msg: String): Unit =
    print(msg)
  def debug(msg: String): Unit =
    print(msg)
  def trace(t: Throwable): Unit =
    t.printStackTrace(System.out)
}
