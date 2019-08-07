package example.sourcecodetest

import cutest.sourcecode.Text
import cutest.SimpleTestSuite

object DebugLite extends SimpleTestSuite {
  test("debug") {
    new Foo(123).bar("lol")  // [param -> arg]: (lol,123)
  }

  def debug[V](value: Text[V]) = {
    println("[" + value.source + "]: " + value.value)
  }

  class Foo(arg: Int){
    debug(arg) // [arg]: 123
    def bar(param: String) = {
      debug(param -> arg)
    }
  }
}
