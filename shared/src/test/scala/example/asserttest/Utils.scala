package example.asserttest

import verify._

object Utils extends Assertion {

  def outputs(rendering: String)(expectation: => Unit): Unit = {
    def normalize(s: String) = augmentString(s.trim()).linesIterator.toList.mkString

    try {
      expectation
      fail("Expectation should have failed but didn't")
    } catch {
      case e: AssertionError =>
        val expected = normalize(rendering)
        val actual = normalize(e.getMessage)
          .replaceAll("@[0-9a-f]*", "@\\.\\.\\.")
          .replaceAll("\u001b\\[[\\d;]*[^\\d;]", "")
        if (actual != expected) {
          throw new AssertionError(s"""Expectation output doesn't match: ${e.getMessage}
                                      |expected = $expected
                                      |actual   = $actual
                                      |""".stripMargin)
        }
    }
  }
}
