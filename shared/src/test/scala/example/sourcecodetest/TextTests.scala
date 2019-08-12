package example.sourcecodetest

import verify.BasicTestSuite

object TextTests extends BasicTestSuite {
  test("foo(1)") {
    assert(foo(1) == ((1, "1")))
  }

  test("foo(bar)") {
    val bar = Seq("lols")
    assert(foo(bar) == ((Seq("lols"), "bar")))
  }

  test("foo(\"lol\")") {
    assert(foo("lol".toString * 2) == (("lollol", "\"lol\".toString * 2")))
  }

  test("foo {...}") {
    assert(foo { println("Hello"); "lol".toString * 2 } == (("lollol", "\"lol\".toString * 2")))
  }

  def foo[T](v: verify.sourcecode.Text[T]) = (v.value, v.source)
}
