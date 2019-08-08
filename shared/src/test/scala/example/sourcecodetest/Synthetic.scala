package example.sourcecodetest

import cutest.sourcecode.{ Enclosing, FullName, Name }
import cutest.BasicTestSuite

object Synthetic extends BasicTestSuite {
  class EnumValue(implicit name: Name.Machine) {
    override def toString = name.value
  }
  object Foo extends EnumValue

  test("EnumValue") {
    assert(Foo.toString == "<init>")
  }

  test("Bar") {
    run()
    ()
  }

  def run() = {
    object Bar {
      assert(
        (Name.Machine() == "<local Bar>") ||
          (Name.Machine() == "<local Bar$>")
      )
      assert(
        (FullName.Machine() == "example.sourcecodetest.Synthetic.Bar.<local Bar>") ||
          (FullName.Machine() == "example.sourcecodetest.Synthetic$._$Bar$.<local Bar$>")
      )
      assert(
        (Enclosing.Machine() == "example.sourcecodetest.Synthetic.run Bar.<local Bar>") ||
          (Enclosing.Machine() == "example.sourcecodetest.Synthetic.run Bar.<local Bar$>")
      )
    }
    Bar
  }
}
