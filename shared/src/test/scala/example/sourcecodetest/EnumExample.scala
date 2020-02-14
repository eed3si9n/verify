package example.sourcecodetest

import verify.sourcecode.Name
import verify._

object EnumExample extends BasicTestSuite {
  case class EnumValue(name: String) {
    override def toString = name
  }
  class Enum {
    def value(implicit name: Name) = EnumValue(name.value)
  }
  object MyEnum extends Enum {
    val firstItem = value
    val secondItem = value
  }

  test("firstItem") {
    assert(MyEnum.firstItem.toString == "firstItem")
  }

  test("secondItem") {
    assert(MyEnum.secondItem.toString == "secondItem")
  }
}
