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

import scala.language.implicitConversions
import cutest.sourcecode.SourceLocation

/** Replacement of `Unit`.
 *
 * Scala automatically converts non-`Unit` values into `Unit`,
 * making it impossible to detect when users are actually
 * returning `Unit` or not in their tests.
 *
 * `Void` on the other hand boxes any such value, such
 * that we can detect it in tests and deliver a meaningful
 * error.
 */
sealed abstract class Void

object Void {

  /** Returns the equivalent of a `Unit`. */
  def unit: Void = UnitRef

  /** The result of a `Unit` to `Void` conversion. */
  case object UnitRef extends Void

  /** Represents a reference that was caught by a conversion. */
  final case class Caught[A](ref: A, location: SourceLocation) extends Void

  /** Implicit conversion that boxes everything except for `Unit`. */
  implicit def toVoid[A](ref: A)(implicit location: SourceLocation): Void =
    ref match {
      case () => Void.UnitRef
      case _  => Void.Caught(ref, location)
    }
}
