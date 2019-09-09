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

  class AssertListener extends RecorderListener[Boolean, Unit] {
    override def expressionRecorded(
        recordedExpr: RecordedExpression[Boolean],
        recordedMessage: Function0[String]
    ): Unit = {
      lazy val rendering: String = new ExpressionRenderer(showTypes = false, shortString = false).render(recordedExpr)
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

  lazy val stringAssertEqualsListener: RecorderListener[String, Unit] = new StringAssertEqualsListener
  class StringAssertEqualsListener extends RecorderListener[String, Unit] {
    val showTypes: Boolean = false
    override def recordingCompleted(recording: Recording[String], recordedMessage: Function0[String]) = {
      recording.recordedExprs match {
        case expected :: found :: Nil =>
          if (expected.value == found.value) ()
          else {
            lazy val rendering: String = new ExpressionRenderer(showTypes = false, shortString = true).render(found)
            val msg = recordedMessage()
            val header =
              "assertion failed" +
                (if (msg == "") ""
                 else ": " + msg)

            val expectedLines = expected.value.linesIterator.toSeq
            val foundLines = found.value.linesIterator.toSeq
            val diff = DiffUtil
              .mkColoredLineDiff(expectedLines, foundLines)
              .linesIterator
              .toSeq
              .map(str => Console.RESET.toString + str)
              .mkString("\n")
            throw new AssertionError(header + "\n\n" + rendering + diff)
          }
        case _ => throw new RuntimeException("unexpected number of expressions " + recording)
      }
    }
  }
}
