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

// one instance per recording
class RecorderRuntime[R, A](listener: RecorderListener[R, A]) {
  protected var recordedValues: List[RecordedValue] = _
  protected var recordedExprs: List[RecordedExpression[R]] = List.empty
  protected var recordedMessage: Function0[String] = () => ""

  def resetValues(): Unit = {
    recordedValues = List.empty
  }

  def recordValue[U](value: U, anchor: Int): U = {
    val recordedValue = RecordedValue(value, anchor)
    listener.valueRecorded(recordedValue)
    recordedValues = recordedValue :: recordedValues
    value
  }

  def recordMessage(message: => String): Unit = {
    recordedMessage = () => message
  }

  def recordExpression(text: String, ast: String, value: R): Unit = {
    val recordedExpr = RecordedExpression(text, ast, value, recordedValues)
    listener.expressionRecorded(recordedExpr, recordedMessage)
    recordedExprs = recordedExpr :: recordedExprs
  }

  def completeRecording(): A = {
    val lastRecorded = recordedExprs.head
    val recording = Recording(lastRecorded.value, recordedExprs)
    listener.recordingCompleted(recording, recordedMessage)
  }
}
