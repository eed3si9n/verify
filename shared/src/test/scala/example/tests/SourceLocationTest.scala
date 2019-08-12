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

package example.tests

import verify.BasicTestSuite
import verify.sourcecode.SourceLocation

object SourceLocationTest extends BasicTestSuite {
  test("implicit SourceLocation works") {
    val pos = implicitly[SourceLocation]
    pos match {
      case SourceLocation("SourceLocationTest.scala", path, 20) =>
        assert(path.contains("SourceLocationTest.scala"))
      case _ =>
        fail(s"Unexpected value: $pos")
    }
  }
}
