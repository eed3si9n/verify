package example.sourcecodetest

import cutest._

object Apply extends SimpleTestSuite {
  val name = sourcecode.Name()
  test("Name") {
    assert(name == "name")
  }

  val fullName = sourcecode.FullName()
  test("FullName") {
    assert(fullName == "example.sourcecodetest.Apply.fullName")
  }

  def someMethod = {
    val enclosing = sourcecode.Enclosing()
    enclosing
  }
  test("Enclosing") {
    assert(someMethod == "example.sourcecodetest.Apply.someMethod enclosing", someMethod)
  }

  val pkg = sourcecode.Pkg()
  test("Pkg") {
    assert(pkg == "example.sourcecodetest")
  }

  val file = sourcecode.SourceFilePath()
  test("SourceFilePath") {
    assert(file.endsWith("/sourcecodetest/Apply.scala"))
  }

  val line = sourcecode.Line()
  test("Line") {
    assert(line == 34)
  }

  test("lazy Name") {
    assert(myLazy.name == "name")
  }

  test("lazy FullName") {
    assert(myLazy.fullName == "example.sourcecodetest.Apply.Bar.fullName", myLazy.fullName)
  }

  test("lazy SourceFilePath") {
    assert(myLazy.file.endsWith("/sourcecodetest/Apply.scala"))
  }

  test("lazy Line") {
    assert(myLazy.line == 68)
  }

  test("lazy Enclosing") {
    assert(
      (myLazy.someMethod == "example.sourcecodetest.Apply.myLazy$lzy Bar#someMethod enclosing") ||
      (myLazy.someMethod == "example.sourcecodetest.Apply.myLazy Bar#someMethod enclosing"), // encoding changed in Scala 2.12
      myLazy.someMethod
    )
  }

  lazy val myLazy = {
    trait Bar{
      val name = sourcecode.Name()
      val fullName = sourcecode.FullName()
      val file = sourcecode.SourceFilePath()
      val line = sourcecode.Line()
      def someMethod = {
        val enclosing = sourcecode.Enclosing()
        enclosing
      }
    }
    val b = new Bar{}
    b
  }
}
