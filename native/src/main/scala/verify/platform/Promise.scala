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

import scala.util.{ Success, Try }

/**
 * Stub needed because Scala Native does not provide an
 * implementation for [[scala.concurrent.Promise]] yet.
 *
 * Note that this isn't a proper `Future` implementation,
 * just something very simple for compilation to work and
 * to pass the current tests.
 */
final class Promise[A] private (private var value: Option[Try[A]] = None) {
  def success(value: A): this.type = {
    this.value = Some(Success(value))
    this
  }

  def future: Future[A] =
    new Future(value.getOrElse(sys.error("not completed")))
}

object Promise {
  def apply[A](): Promise[A] = new Promise[A]()
}
