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
  inline def assertEquals(expected: String, found: String): R =
    ${ StringRecorderMacro.apply('expected, 'found, 'stringAssertEqualsListener) }

  inline def assertEquals(expected: String, found: String, message: => String): R =
    ${ StringRecorderMacro.apply('expected, 'found, 'message, 'stringAssertEqualsListener) }
  }
