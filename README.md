scala-verify
============

scala-verify is a fork of [Minitest](https://github.com/monix/minitest) to prepare for eventual merge into scala/scala.
The purpose of scala-verify is to create a cross-platform, zero-dependency, minimal, testing framework for bootstrapping the toolchain and a small handful of foundational third-party libraries.
See https://github.com/scala/scala-dev/issues/641.

A mini testing framework cross-compiled for Scala 2.11, 2.12,
2.13, Dotty, [Scala.js 0.6.x](http://www.scala-js.org/) and
[Scala Native 0.3.x](https://www.scala-native.org/).

## Usage in sbt

For `build.sbt` (use the `%%%` operator for Scala.js):

```scala
// use the %%% operator for Scala.js
libraryDependencies += "io.monix" %% "minitest" % "2.5.0" % "test"

testFrameworks += new TestFramework("minitest.runner.Framework")
```

## Tutorial

Test suites MUST BE objects, not classes. To create a test suite without `setup` and `teardown`,
extend [BasicTestSuite](shared/src/main/scala/verify/BasicTestSuite.scala) trait:

Here's a simple test:

```scala
import verify._

object SomethingTest extends BasicTestSuite {
  test("should be") {
    assertEquals(2, 1 + 1)
  }

  test("should not be") {
    assert(1 + 1 != 3)
  }

  test("should throw") {
    class DummyException extends RuntimeException("DUMMY")
    def test(): String = throw new DummyException

    intercept[DummyException] {
      test()
    }
  }

  test("test result of") {
    assertResult("hello world") {
      "hello" + " " + "world"
    }
  }
}
```

In case you want to setup an environment for each test example and need `setup` and
`tearDown` semantics, per test example, extend [TestSuite](shared/src/main/scala/verify/TestSuite.scala).
Then on each `test` definition, you'll receive a fresh value:

```scala
import verify.TestSuite

object SomethingTest extends TestSuite[Int] {
  def setup(): Int = {
    Random.nextInt(100) + 1
  }

  def tearDown(env: Int): Unit = {
    assert(env > 0, "should still be positive")
  }

  test("should be positive") { env =>
    assert(env > 0, "positive test")
  }

  test("should be lower or equal to 100") { env =>
    assert(env <= 100, s"$env > 100")
  }
}
```

Some tests require setup and tear down logic to happen only once per test suite
being executed and `TestSuite` supports that as well, but note you should abstain
from doing this unless you really need it, since the per test semantics are much
saner:

```scala
object SomethingTest extends TestSuite[Int] {
  private var system: ActorSystem = _

  override def setupSuite(): Unit = {
    system = ActorSystem.create()
  }

  override def tearDownSuite(): Unit = {
    TestKit.shutdownActorSystem(system)
    system = null
  }
}
```

Minitest supports asynchronous results in tests, use `testAsync` and
return a `Future[Unit]`:

```scala
import scala.concurrent.ExecutionContext.Implicits.global

object SomethingTest extends BasicTestSuite {
  testAsync("asynchronous execution") {
    val future = Future(100).map(_+1)

    for (result <- future) yield {
      assertEquals(result, 101)
    }
  }
}
```

That's all you need to know.

## License

All code in this repository is licensed under the Apache License, Version 2.0.
See [NOTICE](./NOTICE).
