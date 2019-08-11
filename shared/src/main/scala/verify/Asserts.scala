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

import java.util.regex.Matcher
import scala.annotation.tailrec
import scala.reflect.ClassTag
import verify.sourcecode.SourceLocation

trait Asserts {
  def assert(condition: => Boolean)(implicit pos: SourceLocation): Unit = {
    try {
      if (!condition) throw new AssertionException("assertion failed", pos)
    } catch {
      case ex: Throwable =>
        throw new UnexpectedException(ex, pos)
    }
  }

  def assert(condition: => Boolean, hint: String)(implicit pos: SourceLocation): Unit = {

    try {
      if (!condition) throw new AssertionException(hint, pos)
    } catch {
      case ex: Throwable =>
        throw new UnexpectedException(ex, pos)
    }
  }

  def assertResult[T](expected: T)(callback: => T)(implicit pos: SourceLocation): Unit =
    assertResult(expected, "received {0} != expected {1}")(callback)

  def assertResult[T](expected: T, hint: String)(callback: => T)(implicit pos: SourceLocation): Unit = {

    try {
      val rs = callback
      if (rs != expected)
        throw new AssertionException(Asserts.format(hint, rs, expected), pos)
    } catch {
      case ex: Throwable =>
        throw new UnexpectedException(ex, pos)
    }
  }

  def assertEquals[T](received: T, expected: T)(implicit pos: SourceLocation): Unit = {

    if (received != expected)
      throw new AssertionException(Asserts.format("received {0} != expected {1}", received, expected), pos)
  }

  def intercept[E <: Throwable: ClassTag](callback: => Unit)(implicit pos: SourceLocation): Unit = {

    val E = implicitly[ClassTag[E]]
    try {
      callback
      val name = E.runtimeClass.getName
      throw new InterceptException(s"expected a $name to be thrown", pos)
    } catch {
      case ex: InterceptException =>
        throw new AssertionException(ex.getMessage, pos)
      case ex: Throwable if E.runtimeClass.isInstance(ex) =>
        () // Do nothing!
    }
  }

  def cancel()(implicit pos: SourceLocation): Unit =
    throw new CanceledException(None, Some(pos))

  def cancel(reason: String)(implicit pos: SourceLocation): Unit =
    throw new CanceledException(Some(reason), Some(pos))

  def ignore()(implicit pos: SourceLocation): Unit =
    throw new IgnoredException(None, Some(pos))

  def ignore(reason: String)(implicit pos: SourceLocation): Unit =
    throw new IgnoredException(Some(reason), Some(pos))

  def fail()(implicit pos: SourceLocation): Unit =
    throw new AssertionException("failed", pos)

  def fail(reason: String)(implicit pos: SourceLocation): Unit =
    throw new AssertionException(reason, pos)
}

object Asserts extends Asserts {
  def format(tpl: String, values: Any*): String = {
    @tailrec
    def loop(index: Int, acc: String): String =
      if (index >= values.length) acc
      else {
        val value = String.valueOf(values(index))
        val newStr = acc.replaceAll(s"[{]$index[}]", Matcher.quoteReplacement(value))
        loop(index + 1, newStr)
      }

    loop(0, tpl)
  }
}
