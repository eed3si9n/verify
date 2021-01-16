/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package example.asserttest

import verify._
import verify.sourcecode.Compat.isScala3

object RenderingTest extends BasicTestSuite {
  test("literals") {
    outputs("""assertion failed

"abc".length() == 2
      |        |
      3        false
      """) {
      assert {
        "abc".length() == 2
      }
    }
  }

  test("List.apply") {
    val oldStr = """assertion failed

List() == List(1, 2)
       |  |
       |  List(1, 2)
       false
    """
    val newStr = """assertion failed

List() == List(1, 2)
|      |  |
List() |  List(1, 2)
       false
    """
    outputs(oldStr, newStr) {
      assert {
        List() == List(1, 2)
      }
    }
  }

  test("List.apply2") {
    val oldStr = """assertion failed

List(1, 2) == List()
|          |
List(1, 2) false
    """
    val newStr = """assertion failed

List(1, 2) == List()
|          |  |
List(1, 2) |  List()
           false
    """
    outputs(oldStr, newStr) {
      assert {
        List(1, 2) == List()
      }
    }
  }

  test("infix operators") {
    val str = "abc"

    outputs("""assertion failed

str + "def" == "other"
|   |       |
abc abcdef  false
    """) {
      assert {
        str + "def" == "other"
      }
    }
  }

  test("null value") {
    val x = null

    outputs("""assertion failed

x == "null"
| |
| false
null
    """) {
      assert {
        x == "null"
      }
    }
  }

  test("arithmetic expressions") {
    val one = 1

    outputs("""assertion failed

one + 2 == 4
|   |   |
1   3   false
    """) {
      assert {
        one + 2 == 4
      }
    }
  }

  test("field") {
    val person = Person()

    outputs("""assertion failed

person.age == 43
|      |   |
|      42  false
Person(Fred,42)
    """) {
      assert {
        person.age == 43
      }
    }
  }

  test("0-arity apply") {
    val person = Person()
    outputs("""assertion failed

person.doIt() == "pending"
|      |      |
|      done   false
Person(Fred,42)
    """) {
      assert {
        person.doIt() == "pending"
      }
    }
  }

  test("1-arity apply") {
    val person = Person()
    val word = "hey"

    outputs("""assertion failed

person.sayTwice(word) == "hoho"
|      |        |     |
|      heyhey   hey   false
Person(Fred,42)
    """) {
      assert {
        person.sayTwice(word) == "hoho"
      }
    }
  }

  test("2-arity apply") {
    val person = Person()
    val word1 = "hey"
    val word2 = "ho"

    outputs("""assertion failed

person.sayTwo(word1, word2) == "hoho"
|      |      |      |      |
|      heyho  hey    ho     false
Person(Fred,42)
    """) {
      assert {
        person.sayTwo(word1, word2) == "hoho"
      }
    }
  }

  test("varargs apply") {
    val person = Person()
    val word1 = "foo"
    val word2 = "bar"
    val word3 = "baz"

    outputs("""assertion failed

person.sayAll(word1, word2, word3) == "hoho"
|      |      |      |      |      |
|      |      foo    bar    baz    false
|      foobarbaz
Person(Fred,42)
    """) {
      assert {
        person.sayAll(word1, word2, word3) == "hoho"
      }
    }
  }

  test("nested apply") {
    val person = Person()

    outputs("""assertion failed

person.sayTwo(person.sayTwice(person.name), "bar") == "hoho"
|      |      |      |        |      |             |
|      |      |      FredFred |      Fred          false
|      |      Person(Fred,42) Person(Fred,42)
|      FredFredbar
Person(Fred,42)

    """) {
      assert {
        person.sayTwo(person.sayTwice(person.name), "bar") == "hoho"
      }
    }
  }

  test("constructor apply") {
    val brand = "BMW"
    val model = "M5"

    if (isScala3) {
      outputs("""assertion failed

Car(brand, model).brand == "Audi"
|   |      |            |
|   BMW    M5           false
BMW M5
    """) {
        assert {
          Car(brand, model).brand == "Audi"
        }
      }
    } else {
      outputs("""assertion failed

Car(brand, model).brand == "Audi"
|   |      |      |     |
|   BMW    M5     BMW   false
BMW M5
    """) {
        assert {
          Car(brand, model).brand == "Audi"
        }
      }
    }
  }

  test("method apply") {
    outputs("""assertion failed

something(0) == "something1"
|            |
something    false

    """) {
      assert {
        something(0) == "something1"
      }
    }
  }

  test("tuple") {
    if (isScala3) {
      outputs("""assertion failed

(1, 2)._1 == 3
 |     |  |
 (1,2) 1  false
      """) {
        assert {
          (1, 2)._1 == 3
        }
      }
    } else {
      outputs("""assertion failed

(1, 2)._1 == 3
|      |  |
(1,2)  1  false
      """) {
        assert {
          (1, 2)._1 == 3
        }
      }
    }
  }

  test("option") {
    outputs("""assertion failed

Some(23) == Some(22)
|        |  |
Some(23) |  Some(22)
         false
      """) {
      assert {
        Some(23) == Some(22)
      }
    }
  }

  test("message") {
    val person = Person()
    if (isScala3) {
      outputs("""assertion failed: something something

person.age == 43
|      |   |
|      42  false
Person(Fred,42)
      """) {
        assert(person.age == 43, "something something")
      }
    } else {
      outputs("""assertion failed: something something

assert(person.age == 43, "something something")
       |      |   |
       |      42  false
       Person(Fred,42)
      """) {
        assert(person.age == 43, "something something")
      }
    }
  }

  test("long string") {
    val str1 = """virtue! a fig! 'tis in ourselves that we are thus or thus.
    |our bodies are our gardens, to the which our wills are gardeners: so that
    |if we will plant nettles, or sow lettuce, set hyssop and weed up thyme,
    |supply it with one gender of herbs, or distract it with many, either to
    |have it sterile with idleness, or manured with industry, why, the power
    |and corrigible authority of this lies in our wills.""".stripMargin

    val str2 = """a pig! 'tis in ourselves that we are thus or thus.
    |our bodies are our gardens, to the which our wills are gardeners; so that
    |if we will plant nettles, or sow cabbage, set hyssop and weed up thyme,
    |supply it with one gender of herbs, or distract it with many, either to
    |have it sterile with idleness, or manured with industry, why, the power
    |and corrigible authority of this lies in our wills.""".stripMargin

    if (isScala3) {
      outputs(
        """assertion failed: custom message

"virtue! " + str2
           | |
           | a pig! 'tis in ourselves that we are thus or thus....
           virtue! a pig! 'tis in ourselves that we are thus or thus....
virtue! a [fig]! 'tis in ourselves that we are thus or thus.                 |  virtue! a [pig]! 'tis in ourselves that we are thus or thus.
our bodies are our gardens, to the which our wills are gardeners[:] so that  |  our bodies are our gardens, to the which our wills are gardeners[;] so that
if we will plant nettles, or sow [lettuce], set hyssop and weed up thyme,    |  if we will plant nettles, or sow [cabbage], set hyssop and weed up thyme,
supply it with one gender of herbs, or distract it with many, either to      |  supply it with one gender of herbs, or distract it with many, either to
have it sterile with idleness, or manured with industry, why, the power      |  have it sterile with idleness, or manured with industry, why, the power
and corrigible authority of this lies in our wills.                          |  and corrigible authority of this lies in our wills.
      """
      ) {
        assertEquals(str1, "virtue! " + str2, "custom message")
      }
    } else {
      outputs(
        """assertion failed

assertEquals(str1, "virtue! " + str2)
                              | |
                              | a pig! 'tis in ourselves that we are thus or thus....
                              virtue! a pig! 'tis in ourselves that we are thus or thus....
virtue! a [fig]! 'tis in ourselves that we are thus or thus.                 |  virtue! a [pig]! 'tis in ourselves that we are thus or thus.
our bodies are our gardens, to the which our wills are gardeners[:] so that  |  our bodies are our gardens, to the which our wills are gardeners[;] so that
if we will plant nettles, or sow [lettuce], set hyssop and weed up thyme,    |  if we will plant nettles, or sow [cabbage], set hyssop and weed up thyme,
supply it with one gender of herbs, or distract it with many, either to      |  supply it with one gender of herbs, or distract it with many, either to
have it sterile with idleness, or manured with industry, why, the power      |  have it sterile with idleness, or manured with industry, why, the power
and corrigible authority of this lies in our wills.                          |  and corrigible authority of this lies in our wills.
      """
      ) {
        assertEquals(str1, "virtue! " + str2)
      }
    }
  }

  def outputs(renderings: String*)(expectation: => Unit): Unit = {
    def normalize(s: String) = augmentString(s.trim()).linesIterator.toList.mkString

    try {
      expectation
      fail("Expectation should have failed but didn't")
    } catch {
      case e: AssertionError => {
        val expected = renderings.map(normalize)
        val actual = normalize(e.getMessage)
          .replaceAll("@[0-9a-f]*", "@\\.\\.\\.")
          .replaceAll("\u001b\\[[\\d;]*[^\\d;]", "")
        if (!expected.contains(actual)) {
          throw new AssertionError(s"""Expectation output doesn't match: ${e.getMessage}
               |${expected.map(s => "expected = " + s).mkString("\n")}
               |actual   = $actual
               |""".stripMargin)
        }
      }
    }
  }

  def something(x: Int): String = "something"

  case class Person(name: String = "Fred", age: Int = 42) {
    def doIt() = "done"
    def sayTwice(word: String) = word * 2
    def sayTwo(word1: String, word2: String) = word1 + word2
    def sayAll(words: String*) = words.mkString("")
  }

  case class Car(val brand: String, val model: String) {
    override def toString = brand + " " + model
  }

}
