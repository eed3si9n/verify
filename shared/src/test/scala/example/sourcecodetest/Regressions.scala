package example.sourcecodetest

import verify.BasicTestSuite
import verify.sourcecode.Text

object Regressions extends BasicTestSuite {
  test("bug17") {
    val text = Text(Seq(1).map(_ + 1))
    assert(
      (text.source == "Seq(1).map(_ + 1)") ||
        (text.source == "Text(Seq(1).map(_ + 1))")
    )
  }
}
