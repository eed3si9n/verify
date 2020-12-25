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

import collection.mutable.ListBuffer
import collection.immutable.TreeMap

class ExpressionRenderer(showTypes: Boolean, shortString: Boolean) {
  def render(recordedExpr: RecordedExpression[_]): String = {
    val offset = recordedExpr.text.segmentLength(_.isWhitespace, 0)
    val intro = new StringBuilder().append(recordedExpr.text.trim())
    val lines = ListBuffer(new StringBuilder)

    val rightToLeft = filterAndSortByAnchor(recordedExpr.recordedValues)
    for (recordedValue <- rightToLeft) {
      placeValue(lines, recordedValue, math.max(recordedValue.anchor - offset, 0))
    }

    lines.prepend(intro)
    lines.append(new StringBuilder)

    // debug
    // recordedExpr.recordedValues foreach { v =>
    //   val line = new StringBuilder()
    //   line.append(v.toString)
    //   lines.append(line)
    // }
    lines.mkString("\n")
  }

  private[this] def filterAndSortByAnchor(recordedValues: List[RecordedValue[_]]): Iterable[RecordedValue[_]] = {
    var map = TreeMap[Int, RecordedValue[_]]()(Ordering.by(-_))
    // values stemming from compiler generated code often have the same anchor as regular values
    // and get recorded before them; let's filter them out
    for { value <- recordedValues } {
      if (!map.contains(value.anchor)) map += (value.anchor -> value)
    }
    map.values
  }

  private[this] def placeValue[T](lines: ListBuffer[StringBuilder], recordedValue: RecordedValue[T], col: Int): Unit = {
    val str = renderValue(recordedValue)

    placeString(lines(0), "|", col)

    import util.control.Breaks._
    breakable {
      for (line <- lines.drop(1)) {
        if (fits(line, str, col)) {
          placeString(line, str, col)
          break()
        }
        placeString(line, "|", col)
      }

      val newLine = new StringBuilder()
      placeString(newLine, str, col)
      lines.append(newLine)
    }
  }

  private[this] def renderValue[T](recordedValue: RecordedValue[T]): String = {
    val strShow = recordedValue.showValue
    val strShort =
      if (!shortString) strShow
      else if (strShow.contains("\n")) strShow.linesIterator.toList.headOption.getOrElse("") + "..."
      else strShow
    if (showTypes) strShort + " (" + recordedValue.value.getClass.getName + ")" // TODO: get type name the Scala way
    else strShort
  }

  private[this] def placeString(line: StringBuilder, str: String, anchor: Int): Unit = {
    val diff = anchor - line.length
    for (i <- 1 to diff) line.append(' ')
    line.replace(anchor, anchor + str.length(), str)
  }

  private[this] def fits(line: StringBuilder, str: String, anchor: Int): Boolean = {
    line.slice(anchor, anchor + str.length() + 1).forall(_.isWhitespace)
  }
}
