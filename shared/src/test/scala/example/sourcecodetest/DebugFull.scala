package example.sourcecodetest

import cutest.SimpleTestSuite

object DebugFull extends SimpleTestSuite {
  test("debug") {
    new Foo(123).bar("lol")  // example.sourcecodetest.DebugFull.Foo#bar [param -> arg]: (lol,123)
    ()
  }

  class Foo(arg: Int){
    debug(arg) // example.sourcecodetest.DebugFull.Foo [arg]: 123
    def bar(param: String) = {
      debug(param -> arg)
    }
  }

  def debug[V](value: cutest.sourcecode.Text[V])(implicit enclosing: cutest.sourcecode.Enclosing) = {
    println(enclosing.value + " [" + value.source + "]: " + value.value)
  }
}
