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

package cutest.api

import scala.util.control.NonFatal

object Utils {
  def silent[T](cb: => T): Unit =
    try {
      cb; ()
    } catch {
      case NonFatal(_) => ()
    }
}
