package example.sourcecodetest

import cutest.SimpleTestSuite
import cutest.sourcecode.Text

object Regressions extends SimpleTestSuite {
  test("bug17") {
    val text = Text(Seq(1).map(_+1))
    assert(text.source == "Seq(1).map(_+1)")
  }
}
