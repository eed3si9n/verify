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

package example.asserttest

import example.asserttest.Utils.outputs
import verify._

object ShowTest extends BasicTestSuite {
  test("default show toString") {
    outputs("""assertion failed

"abc".length == 2
      |      |
      3      false
    """) {
      assert {
        "abc".length == 2
      }
    }
  }

  test("custom show") {
    import CustomShows._
    outputs("""assertion failed

"abc".length() == 2
      |        |
      Int(3)   false
    """) {

      assert {
        "abc".length() == 2
      }
    }
  }
}

object CustomShows {
  implicit val customIntShow: Show[Int] = new Show[Int] {
    override def show(i: Int): String = s"Int($i)"
  }
}
