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


package cutest
package sourcecode

private[sourcecode] trait SourceValue[T]{
  def value: T
}
private[sourcecode] trait SourceCompanion[T, V <: SourceValue[T]](build: T => V){
  def apply() given V: T = the[V].value
}

case class Name(value: String) extends SourceValue[String]
object Name extends NameMacros with SourceCompanion[String, Name](new Name(_)) {
  case class Machine(value: String) extends SourceValue[String]
  object Machine extends NameMachineMacros with SourceCompanion[String, Machine](new Machine(_))
}

case class FullName(value: String) extends SourceValue[String]
object FullName extends FullNameMacros with SourceCompanion[String, FullName](new FullName(_)) {
  case class Machine(value: String) extends SourceValue[String]
  object Machine extends FullNameMachineMacros with SourceCompanion[String, Machine](new Machine(_))
}

case class SourceFileName(value: String) extends SourceValue[String]
object SourceFileName extends SourceFileNameMacros with SourceCompanion[String, SourceFileName](new SourceFileName(_))

case class SourceFilePath(value: String) extends SourceValue[String]
object SourceFilePath extends SourceFilePathMacros with SourceCompanion[String, SourceFilePath](new SourceFilePath(_))

case class Line(value: Int) extends SourceValue[Int]
object Line extends LineMacros with SourceCompanion[Int, Line](new Line(_))

case class Enclosing(value: String) extends SourceValue[String]
object Enclosing extends EnclosingMacros with SourceCompanion[String, Enclosing](new Enclosing(_)) {
  case class Machine(value: String) extends SourceValue[String]
  object Machine extends EnclosingMachineMacros with SourceCompanion[String, Machine](new Machine(_))
}

case class Pkg(value: String) extends SourceValue[String]
object Pkg extends PkgMacros with SourceCompanion[String, Pkg](new Pkg(_))

case class Text[T](value: T, source: String)
object Text extends TextMacros

case class Args(value: Seq[Seq[Text[_]]]) extends SourceValue[Seq[Seq[Text[_]]]]
object Args extends ArgsMacros with SourceCompanion[Seq[Seq[Text[_]]], Args](new Args(_))
