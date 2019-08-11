package example.sourcecodetest

import verify.BasicTestSuite
import verify.sourcecode.{ Enclosing, Text }

object DebugFull extends BasicTestSuite {
  test("debug") {
    new Foo(123).bar("lol") // example.sourcecodetest.DebugFull.Foo#bar [param -> arg]: (lol,123)
    ()
  }

  class Foo(arg: Int) {
    debug(arg) // example.sourcecodetest.DebugFull.Foo [arg]: 123
    def bar(param: String) = {
      debug(param -> arg)
    }
  }

  def debug[V](value: Text[V])(implicit enclosing: Enclosing) = {
    println(enclosing.value + " [" + value.source + "]: " + value.value)
  }
}
