package example.sourcecodetest

import cutest.sourcecode.FullName
import cutest._

object EnumFull extends SimpleTestSuite {
  case class EnumValue(name: String) {
    override def toString = name
  }
  class Enum {
    def value(implicit name: FullName) = EnumValue(name.value)
  }
  object MyEnum extends Enum {
    val firstItem = value
    val secondItem = value
  }

  test("firstItem") {
    assert(MyEnum.firstItem.toString == "example.sourcecodetest.EnumFull.MyEnum.firstItem")
  }

  test("secondItem") {
    assert(MyEnum.secondItem.toString == "example.sourcecodetest.EnumFull.MyEnum.secondItem")
  }
}
