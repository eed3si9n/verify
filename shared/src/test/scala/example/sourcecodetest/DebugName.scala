package example.sourcecodetest

import cutest.sourcecode.{ Name, Text }
import cutest.SimpleTestSuite

object DebugName extends SimpleTestSuite {
  test("debug") {
    new Foo(123).bar("lol") // bar [param -> arg]: (lol,123)
    ()
  }

  def debug[V](value: Text[V])(implicit name: Name) = {
    println(name.value + " [" + value.source + "]: " + value.value)
  }

  class Foo(arg: Int) {
    debug(arg) // Foo [arg]: 123
    def bar(param: String) = {
      debug(param -> arg)
    }
  }
}
