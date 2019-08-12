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
package sourcecode

abstract class SourceValue[T] {
  def value: T
}
abstract class SourceCompanion[T, V <: SourceValue[T]](build: T => V) {
  import scala.language.implicitConversions

  def apply()(implicit s: V): T = s.value
  implicit def toScalaVerifySourcecodeSourceValue(s: T): V = build(s)
}

case class Name(value: String) extends SourceValue[String]
object Name extends SourceCompanion[String, Name](new Name(_)) with NameMacros {
  case class Machine(value: String) extends SourceValue[String]
  object Machine extends SourceCompanion[String, Machine](new Machine(_)) with NameMachineMacros
}

case class FullName(value: String) extends SourceValue[String]
object FullName extends SourceCompanion[String, FullName](new FullName(_)) with FullNameMacros {
  case class Machine(value: String) extends SourceValue[String]
  object Machine extends SourceCompanion[String, Machine](new Machine(_)) with FullNameMachineMacros
}

case class SourceFileName(value: String) extends SourceValue[String]
object SourceFileName extends SourceCompanion[String, SourceFileName](new SourceFileName(_)) with SourceFileNameMacros

case class SourceFilePath(value: String) extends SourceValue[String]
object SourceFilePath extends SourceCompanion[String, SourceFilePath](new SourceFilePath(_)) with SourceFilePathMacros

case class Line(value: Int) extends SourceValue[Int]
object Line extends SourceCompanion[Int, Line](new Line(_)) with LineMacros

case class SourceLocation(fileName: String, filePath: String, line: Int)
object SourceLocation {
  implicit def toScalaVerifySourcecodeSourceLocation(
      implicit n: SourceFileName,
      p: SourceFilePath,
      l: Line
  ): SourceLocation =
    SourceLocation(n.value, p.value, l.value)
  def apply()(implicit s: SourceLocation): SourceLocation = s
}

case class Enclosing(value: String) extends SourceValue[String]

object Enclosing extends SourceCompanion[String, Enclosing](new Enclosing(_)) with EnclosingMacros {
  case class Machine(value: String) extends SourceValue[String]
  object Machine extends SourceCompanion[String, Machine](new Machine(_)) with EnclosingMachineMacros
}

case class Pkg(value: String) extends SourceValue[String]
object Pkg extends SourceCompanion[String, Pkg](new Pkg(_)) with PkgMacros

case class Text[T](value: T, source: String)
object Text extends TextMacros

/*
case class Args(value: Seq[Seq[Text[_]]]) extends SourceValue[Seq[Seq[Text[_]]]]
object Args extends SourceCompanion[Seq[Seq[Text[_]]], Args](new Args(_)) with ArgsMacros
 */
