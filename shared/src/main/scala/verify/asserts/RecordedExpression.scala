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

// might hold more information in the future (for example the kind of expression),
// or might be turned into an expression tree
case class RecordedExpression[T](text: String, ast: String, value: T, recordedValues: List[RecordedValue]) {}
