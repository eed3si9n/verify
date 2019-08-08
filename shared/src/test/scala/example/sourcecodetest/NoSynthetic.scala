package example.sourcecodetest

import cutest.sourcecode.{ Enclosing, FullName, Name }
import cutest.BasicTestSuite

object NoSynthetic extends BasicTestSuite {
  class EnumValue(implicit name: Name) {
    override def toString = name.value
  }
  object Foo extends EnumValue

  test("EnumValue") {
    assert(Foo.toString == "Foo")
  }

  test("Bar") {
    run()
    ()
  }

  def run() = {
    object Bar {
      assert(Name() == "Bar")
      assert(FullName() == "example.sourcecodetest.NoSynthetic.Bar")
      assert(Enclosing() == "example.sourcecodetest.NoSynthetic.run Bar")
    }
    Bar
  }
}
