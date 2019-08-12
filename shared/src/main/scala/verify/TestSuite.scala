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

trait TestSuite[Env] extends AbstractTestSuite with Assertion {
  private[this] implicit lazy val ec: ExecutionContext = executionContext

  def setupSuite(): Unit = ()
  def tearDownSuite(): Unit = ()
  def setup(): Env
  def tearDown(env: Env): Unit

  def test(name: String)(f: Env => Void): Unit =
    synchronized {
      if (isInitialized) throw initError()
      propertiesSeq = propertiesSeq :+
        TestSpec.sync[Env](name, env => f(env))
    }

  def testAsync(name: String)(f: Env => Future[Unit]): Unit =
    synchronized {
      if (isInitialized) throw initError()
      propertiesSeq = propertiesSeq :+
        TestSpec.async[Env](name, f)
    }

  lazy val properties: Properties[_] =
    synchronized {
      if (!isInitialized) isInitialized = true
      Properties(setup _, (env: Env) => { tearDown(env); Void.UnitRef }, setupSuite _, tearDownSuite _, propertiesSeq)
    }

  def executionContext: ExecutionContext = ExecutionContext.global

  private[this] var propertiesSeq = Seq.empty[TestSpec[Env, Unit]]
  private[this] var isInitialized = false
  private[this] def initError() =
    new AssertionError(
      "Cannot define new tests after TestSuite was initialized"
    )
}
