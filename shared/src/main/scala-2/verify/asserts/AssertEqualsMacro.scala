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

trait AssertEquals[R] {
  protected def stringAssertEqualsListener: RecorderListener[String, R]
  def assertEquals(expected: String, found: String): R = macro StringRecorderMacro.apply[String, R]
  // def apply(value: A, message: => String): R = macro RecorderMacro.apply[A, R]
}
