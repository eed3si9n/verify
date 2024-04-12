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

import verify.TestSuite
import scala.concurrent.Future
import scala.util.Random

object EnvironmentTest extends TestSuite[Int] {
  def setup(): Int = {
    Random.nextInt(100) + 1
  }

  def tearDown(env: Int): Unit = {
    assert(env > 0)
  }

  override def setupSuite() = {}

  override def tearDownSuite() = {}

  test("simple test") { env =>
    assert(env == env)
  }

  testAsync("asynchronous test") { env =>
    implicit val ec = verify.platform.defaultExecutionContext

    Future(env).map(_ + 1).map { result =>
      assert(result == env + 1)
    }
  }
}
