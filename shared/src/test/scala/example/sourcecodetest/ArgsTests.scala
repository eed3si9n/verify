package example.sourcecodetest

import cutest.SimpleTestSuite

object ArgsTests extends SimpleTestSuite {
  var args: Seq[Seq[(String, Any)]] = Seq()

  def debug(implicit arguments: cutest.sourcecode.Args): Unit =
    args = arguments.value.map(_.map(t => t.source -> t.value))

  def foo(p1: String, p2: Long, p3: Boolean)(foo: String, bar: String): Unit = {
    debug
  }

  def bar(p1: String, p2: Long, p3: Boolean)(foo: String, bar: String): Unit = {
    val bar = {
      debug
      "bar"
    }
  }

  def baz: Unit = {
    debug
  }

  def withImplicit(p1: String, p2: Long, p3: Boolean)(implicit foo: String): Unit = {
    debug
  }

  class Foo(p1: String, p2: Long, p3: Boolean)(foo: String, bar: String) {
    debug

    def this(p1: String, p2: Long) = {
      this(p1, p2, false)("foo", "bar")
      debug
    }
  }

  test("Foo") {
    new Foo("text", 42, false)("foo", "bar")
    assert(args == Seq(Seq("p1" -> "text", "p2" -> 42, "p3" -> false), Seq("foo" -> "foo", "bar" -> "bar")))
  }

  test("Foo") {
    new Foo("text", 42)
    assert(args == Seq(Seq("p1" -> "text", "p2" -> 42)))
  }

  test("Foo") {
    foo("text", 42, false)("foo", "bar")
    assert(args == Seq(Seq("p1" -> "text", "p2" -> 42, "p3" -> false), Seq("foo" -> "foo", "bar" -> "bar")))
  }

  test("bar") {
    bar("text", 42, false)("foo", "bar")
    assert(args == Seq(Seq("p1" -> "text", "p2" -> 42, "p3" -> false), Seq("foo" -> "foo", "bar" -> "bar")))
  }

  test("baz") {
    baz
    assert(args == Seq())
  }

  test("withImplicit") {
    withImplicit("text", 42, false)("foo")
    assert(args == Seq(Seq("p1" -> "text", "p2" -> 42, "p3" -> false), Seq("foo" -> "foo")))
  }

  test("withImplicit") {
    implicit val implicitFoo = "bar"
    withImplicit("text", 42, false)
    assert(args == Seq(Seq("p1" -> "text", "p2" -> 42, "p3" -> false), Seq("foo" -> "bar")))
  }
}
