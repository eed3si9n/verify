package example.sourcecodetest

import cutest.SimpleTestSuite
import cutest.sourcecode._

object Implicits extends SimpleTestSuite {
  val name = implicitly[Name]
  test("Name") {
    assert(name.value == "name")
  }

  val fullName = implicitly[FullName]
  test("FullName") {
    assert(fullName.value == "example.sourcecodetest.Implicits.fullName")
  }

  def someMethod = {
    val enclosing = implicitly[Enclosing]
    enclosing
  }
  test("Enclosing") {
    assert(someMethod.value == "example.sourcecodetest.Implicits.someMethod enclosing")
  }

  val pkg = implicitly[Pkg]
  test("Pkg") {
    assert(pkg.value == "example.sourcecodetest")
  }

  val file = implicitly[SourceFilePath]
  test("SourceFilePath") {
    assert(file.value.endsWith("/sourcecodetest/Implicits.scala"))
  }

  val line = implicitly[Line]
  test("Line") {
    assert(line.value == 35)
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
    assert(myLazy.line.value == 69)
  }

  test("lazy Enclosing") {
    assert(
      (myLazy.someMethod.value == "example.sourcecodetest.Implicits.myLazy$lzy Bar#someMethod enclosing") ||
        (myLazy.someMethod.value == "example.sourcecodetest.Implicits.myLazy Bar#someMethod enclosing"), // encoding changed in Scala 2.12
      myLazy.someMethod.value
    )
  }

  lazy val myLazy: BarApi = {
    trait Bar extends BarApi {
      override val name = implicitly[Name]
      override val fullName = implicitly[FullName]
      override val file = implicitly[SourceFilePath]
      override val line = implicitly[Line]
      override def someMethod = {
        val enclosing = implicitly[Enclosing]
        enclosing
      }
    }
    val b = new Bar {}
    b
  }

  trait BarApi {
    def name: Name
    def fullName: FullName
    def file: SourceFilePath
    def line: Line
    def someMethod: Enclosing
  }
}
