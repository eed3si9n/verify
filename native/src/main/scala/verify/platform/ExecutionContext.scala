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

/**
 * Stub needed because Scala Native does not provide an
 * implementation for [[scala.concurrent.ExecutionContext]] yet.
 *
 * Note that this isn't a proper `ExecutionContext` implementation,
 * just something very simple for compilation to work and
 * to pass the current tests.
 */
trait ExecutionContext

object ExecutionContext {
  val global: ExecutionContext = new ExecutionContext {}

  object Implicits {
    implicit val global: ExecutionContext = ExecutionContext.global
  }
}
