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
package asserts

import language.experimental.macros

abstract class Recorder[A, R] {
  protected def listener: RecorderListener[A, R]
  inline def apply(value: A): R =
    ${ RecorderMacro.apply('value, 'listener) }
  inline def apply(value: A, message: => String): R =
    ${ RecorderMacro.apply('value, 'message, 'listener) }
}
