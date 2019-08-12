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

trait RecorderListener[T, A] {
  def valueRecorded(recordedValue: RecordedValue): Unit = {}
  def expressionRecorded(recordedExpr: RecordedExpression[T], recordedMessage: Function0[String]): Unit = {}
  def recordingCompleted(recording: Recording[T], recordedMessage: Function0[String]): A
}
