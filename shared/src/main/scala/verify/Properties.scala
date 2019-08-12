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

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NonFatal
import verify.Utils.silent

case class Properties[I](
    setup: () => I,
    tearDown: I => Void,
    setupSuite: () => Unit,
    tearDownSuite: () => Unit,
    properties: Seq[TestSpec[I, Unit]]
)(implicit ec: ExecutionContext)
    extends Iterable[TestSpec[Unit, Unit]] {

  def iterator: Iterator[TestSpec[Unit, Unit]] = {
    for (property <- properties.iterator)
      yield TestSpec[Unit, Unit](
        property.name, { _ =>
          try {
            val env = setup()
            val result = try property(env)
            catch {
              case NonFatal(ex) =>
                Future.successful(Result.from(ex))
            }

            result.flatMap {
              case Result.Success(_) =>
                TestSpec.sync(property.name, tearDown)(env)
              case error =>
                silent(tearDown(env))
                Future.successful(error)
            }
          } catch {
            case NonFatal(ex) =>
              Future.successful(Result.from(ex))
          }
        }
      )
  }
}
