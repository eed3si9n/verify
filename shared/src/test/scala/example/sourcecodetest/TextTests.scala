package example.sourcecodetest

import cutest.SimpleTestSuite

object TextTests extends SimpleTestSuite {
  test("foo(1)") {
    assert(foo(1) == (1, "1"))
  }

  test("foo(bar)") {
    val bar = Seq("lols")
    assert(foo(bar) == (Seq("lols"), "bar"))
  }

  test("foo(\"lol\")") {
    assert(foo("lol".toString * 2) == ("lollol", "\"lol\".toString * 2"))
  }

  test("foo {...}") {
    assert(foo{println("Hello"); "lol".toString * 2} == ("lollol", "\"lol\".toString * 2"))
  }

  def foo[T](v: cutest.sourcecode.Text[T]) = (v.value, v.source)
}
