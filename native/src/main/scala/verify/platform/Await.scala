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

package verify
package platform

import scala.concurrent.duration.Duration

/**
 * Stub needed because Scala Native does not provide an
 * implementation for [[scala.concurrent.Await]] yet.
 *
 * Note that this isn't a proper `Await` implementation,
 * just something very simple for compilation to work and
 * to pass the current tests.
 */
object Await {
  def result[A](future: Future[A], duration: Duration): A =
    future.value.get
}
