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

import scala.util.{ Failure, Success, Try }

/**
 * Stub needed because Scala Native does not provide an
 * implementation for [[scala.concurrent.Future]] yet.
 *
 * Note that this isn't a proper `Future` implementation,
 * just something very simple for compilation to work and
 * to pass the current tests.
 */
final class Future[+A] private[verify] (private[verify] val value: Try[A]) {
  def map[B](f: A => B)(implicit executor: ExecutionContext): Future[B] =
    new Future(value.map(f))

  def flatMap[B](f: A => Future[B])(implicit executor: ExecutionContext): Future[B] =
    new Future(value.flatMap(f andThen (_.value)))

  def onComplete[U](f: Try[A] => U)(implicit executor: ExecutionContext): Unit =
    f(value)
}

object Future {
  def apply[A](f: => A): Future[A] =
    new Future(Try(f))

  def successful[A](value: A): Future[A] =
    new Future(Success(value))

  def failed[A](e: Throwable): Future[A] =
    new Future(Failure(e))
}
