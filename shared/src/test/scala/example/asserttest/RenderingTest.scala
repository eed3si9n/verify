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
import verify.sourcecode.Compat.isDotty

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
    if (isDotty) {
      outputs("""assertion failed

List() == List(1, 2)
|      |  |
List() |  List(1, 2)
       false
    """) {
        assert {
          List() == List(1, 2)
        }
      }
    } else {
      outputs("""assertion failed

List() == List(1, 2)
       |  |
       |  List(1, 2)
       false
    """) {
        assert {
          List() == List(1, 2)
        }
      }
    }
  }

  test("List.apply2") {
    if (isDotty) {
      outputs("""assertion failed

List(1, 2) == List()
|          |  |
List(1, 2) |  List()
           false
    """) {
        assert {
          List(1, 2) == List()
        }
      }
    } else {
      outputs("""assertion failed

List(1, 2) == List()
|          |
List(1, 2) false
    """) {
        assert {
          List(1, 2) == List()
        }
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

    if (isDotty) {
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

  test("tuple") {
    if (isDotty) {
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
    if (isDotty) {
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

  def outputs(rendering: String)(expectation: => Unit): Unit = {
    def normalize(s: String) = augmentString(s.trim()).linesIterator.toList.mkString

    try {
      expectation
      fail("Expectation should have failed but didn't")
    } catch {
      case e: AssertionError => {
        val expected = normalize(rendering)
        val actual = normalize(e.getMessage).replaceAll("@[0-9a-f]*", "@\\.\\.\\.")
        if (actual != expected) {
          throw new AssertionError(s"""Expectation output doesn't match: ${e.getMessage}
               |expected = $expected
               |actual   = $actual
               |""".stripMargin)
        }
      }
    }
  }

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
