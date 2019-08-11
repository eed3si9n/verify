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

import verify.platform.{ ExecutionContext, Future, Promise }
import scala.util.control.NonFatal
import scala.util.{ Failure, Success }
import verify.sourcecode.SourceLocation

case class TestSpec[I, +O](name: String, f: I => Future[Result[O]]) extends (I => Future[Result[O]]) {

  override def apply(v1: I): Future[Result[O]] = f(v1)
}

object TestSpec {
  def async[Env](name: String, cb: Env => Future[Unit])(implicit ec: ExecutionContext): TestSpec[Env, Unit] =
    TestSpec(
      name, { env =>
        val f: Future[Unit] =
          try cb(env)
          catch { case NonFatal(ex) => Future.failed(ex) }

        val p = Promise[Result[Unit]]()
        f.onComplete {
          case Success(_) =>
            p.success(Result.Success(()))
          case Failure(ex) =>
            p.success(Result.from(ex))
        }
        p.future
      }
    )

  def sync[Env](name: String, cb: Env => Void): TestSpec[Env, Unit] =
    TestSpec(
      name, { env =>
        try {
          cb(env) match {
            case Void.UnitRef =>
              Future.successful(Result.Success(()))
            case Void.Caught(ref, loc) =>
              Future.successful(unexpected(ref, loc))
          }
        } catch {
          case NonFatal(ex) =>
            Future.successful(Result.from(ex))
        }
      }
    )

  private def unexpected[A](ref: A, loc: SourceLocation): Result[Nothing] =
    Result.Failure(
      s"Problem with test spec, expecting `Unit`, but received: $ref ",
      None,
      Some(loc)
    )
}
