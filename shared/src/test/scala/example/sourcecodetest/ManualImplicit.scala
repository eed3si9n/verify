package example.sourcecodetest

import verify.sourcecode.{ Enclosing, FullName, Line, Name }
import verify.BasicTestSuite

object ManualImplicit extends BasicTestSuite {
  test("Name") {
    def apply() = {
      assert(foo() == "apply")
    }
    apply()
    assert(foo()(Name("cow")) == "cow")
  }

  test("Line") {
    assert(bar() == 16)
    assert(bar()(Line(123)) == 123)
    assert(bar()(Line(123)) == 123)
  }

  test("FullName") {
    def apply() = {
      assert(baz() == "example.sourcecodetest.ManualImplicit.apply", baz())
      assert(baz() == "example.sourcecodetest.ManualImplicit.apply")
    }
    apply()
  }

  test("Enclosing") {
    def apply() = {
      def enc() =
        assert(qux() == "example.sourcecodetest.ManualImplicit.apply enc")
      enc()
      def enc2() =
        assert(
          qux()(Enclosing("example.sourcecodetest.ManualImplicit"))
            == "example.sourcecodetest.ManualImplicit"
        )

      enc2()
    }
    apply()
  }

  def foo()(implicit i: Name) = i.value
  def bar()(implicit i: Line) = i.value
  def baz()(implicit i: FullName) = i.value
  def qux()(implicit i: Enclosing) = i.value
}
