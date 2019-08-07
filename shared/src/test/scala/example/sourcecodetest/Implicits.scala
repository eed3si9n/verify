package example.sourcecodetest

import cutest.{ SimpleTestSuite, sourcecode }

object Implicits extends SimpleTestSuite {
  val name = implicitly[sourcecode.Name]
  test("Name") {
    assert(name.value == "name")
  }

  val fullName = implicitly[sourcecode.FullName]
  test("FullName") {
    assert(fullName.value == "example.sourcecodetest.Implicits.fullName")
  }

  def someMethod = {
    val enclosing = implicitly[sourcecode.Enclosing]
    enclosing
  }
  test("Enclosing") {
    assert(someMethod.value == "example.sourcecodetest.Implicits.someMethod enclosing")
  }

  val pkg = implicitly[sourcecode.Pkg]
  test("Pkg") {
    assert(pkg.value == "example.sourcecodetest")
  }

  val file = implicitly[sourcecode.SourceFilePath]
  test("SourceFilePath") {
    assert(file.value.endsWith("/sourcecodetest/Implicits.scala"))
  }

  val line = implicitly[sourcecode.Line]
  test("Line") {
    assert(line.value == 34)
  }

  test("lazy Name") {
    assert(myLazy.name.value == "name")
  }

  test("lazy FullName") {
    assert(myLazy.fullName.value == "example.sourcecodetest.Implicits.Bar.fullName", myLazy.fullName.value)
  }

  test("lazy SourceFilePath") {
    assert(myLazy.file.value.endsWith("/sourcecodetest/Implicits.scala"))
  }

  test("lazy Line") {
    assert(myLazy.line.value == 68)
  }

  test("lazy Enclosing") {
    assert(
      (myLazy.someMethod.value == "example.sourcecodetest.Implicits.myLazy$lzy Bar#someMethod enclosing") ||
      (myLazy.someMethod.value == "example.sourcecodetest.Implicits.myLazy Bar#someMethod enclosing"), // encoding changed in Scala 2.12
      myLazy.someMethod.value
    )
  }

  lazy val myLazy = {
    trait Bar{
      val name = implicitly[sourcecode.Name]
      val fullName = implicitly[sourcecode.FullName]
      val file = implicitly[sourcecode.SourceFilePath]
      val line = implicitly[sourcecode.Line]
      def someMethod = {
        val enclosing = implicitly[sourcecode.Enclosing]
        enclosing
      }
    }
    val b = new Bar{}
    b
  }
  myLazy
}
