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

/**
 * An instance of PowerAssert returns an object that can be called
 * with a signature of `assert(...)` function.
 */
class PowerAssert extends Recorder[Boolean, Unit] {
  val failEarly: Boolean = true
  val showTypes: Boolean = false

  class AssertListener extends RecorderListener[Boolean, Unit] {
    override def expressionRecorded(
        recordedExpr: RecordedExpression[Boolean],
        recordedMessage: Function0[String]
    ): Unit = {
      lazy val rendering: String = new ExpressionRenderer(showTypes).render(recordedExpr)
      // println(recordedExpr.ast + "\n")
      // if (printExprs) println(rendering)
      if (!recordedExpr.value && failEarly) {
        val msg = recordedMessage()
        val header =
          "assertion failed" +
            (if (msg == "") ""
             else ": " + msg)
        throw new AssertionError(header + "\n\n" + rendering)
      }
    }

    override def recordingCompleted(recording: Recording[Boolean], recordedMessage: Function0[String]) = {}
  }

  override lazy val listener: RecorderListener[Boolean, Unit] = new AssertListener
}

object PowerAssert {
  lazy val assert: PowerAssert = new PowerAssert()
}
