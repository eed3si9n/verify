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

package verify
package asserts

import scala.quoted._

class RecorderMacro(using qctx0: Quotes) {
  // https://github.com/lampepfl/dotty/releases/tag/3.0.0-M2
  // https://dotty.epfl.ch/docs/reference/metaprogramming/tasty-reflect.html#sealing-and-unsealing
  import qctx0.reflect._
  import util._

  private[this] val runtimeSym: Symbol = TypeRepr.of[RecorderRuntime[_, _]].typeSymbol

  def apply[A: Type, R: Type](
      recording: Expr[A],
      message: Expr[String],
      listener: Expr[RecorderListener[A, R]]): Expr[R] = {
    val termArg: Term = Term.of(recording).underlyingArgument

    '{
      val recorderRuntime: RecorderRuntime[A, R] = new RecorderRuntime($listener)
      recorderRuntime.recordMessage($message)
      ${
        Block(
          recordExpressions(Term.of('{ recorderRuntime }), termArg),
          Term.of('{ recorderRuntime.completeRecording() })
        ).asExprOf[R]
      }
    }
  }

  def apply2[A: Type, R: Type](
      expected: Expr[A],
      found: Expr[A],
      message: Expr[String],
      listener: Expr[RecorderListener[A, R]]): Expr[R] = {
    val expectedArg: Term = Term.of(expected).underlyingArgument
    val foundArg: Term = Term.of(found).underlyingArgument

    '{
      val recorderRuntime: RecorderRuntime[A, R] = new RecorderRuntime($listener)
      recorderRuntime.recordMessage($message)
      ${
        Block(
          recordExpressions(Term.of('{ recorderRuntime }), expectedArg) :::
          recordExpressions(Term.of('{ recorderRuntime }), foundArg),
          Term.of('{ recorderRuntime.completeRecording() })
        ).asExprOf[R]
      }
    }
  }

  private[this] def recordExpressions(runtime: Term, recording: Term): List[Term] = {
    val source = getSourceCode(recording)
    val ast = recording.showExtractors

    val resetValuesSel: Term = {
      val m = runtimeSym.method("resetValues").head
      runtime.select(m)
    }
    try {
      List(
        Apply(resetValuesSel, List()),
        recordExpression(runtime, source, ast, recording)
      )
    } catch {
      case e: Throwable => throw new RuntimeException(
        "Expecty: Error rewriting expression.\nText: " + source + "\nAST : " + ast, e)
    }
  }

  // emit recorderRuntime.recordExpression(<source>, <tree>, instrumented)
  private[this] def recordExpression(runtime: Term, source: String, ast: String, expr: Term): Term = {
    val instrumented = recordAllValues(runtime, expr)
    val recordExpressionSel: Term = {
      val m = runtimeSym.method("recordExpression").head
      runtime.select(m)
    }
    Apply(recordExpressionSel,
      List(
        Literal(Constant.String(source)),
        Literal(Constant.String(ast)),
        instrumented
      ))
  }

  private[this] def recordAllValues(runtime: Term, expr: Term): Term =
    expr match {
      case New(_)     => expr
      case Literal(_) => expr
      case Typed(r @ Repeated(xs, y), tpe) => Typed.copy(r)(recordSubValues(runtime, r), tpe)
      // don't record value of implicit "this" added by compiler; couldn't find a better way to detect implicit "this" than via point
      case Select(x@This(_), y) if expr.pos.start == x.pos.start => expr
      // case x: Select if x.symbol.isModule => expr // don't try to record the value of packages
      case _ => recordValue(runtime, recordSubValues(runtime, expr), expr)
    }

  private[this] def recordSubValues(runtime: Term, expr: Term): Term =
    expr match {
      case Apply(x, ys) =>
        try {
          Apply(recordAllValues(runtime, x), ys.map(recordAllValues(runtime, _)))
        } catch {
          case e: AssertionError => expr
        }
      // case TypeApply(x, ys) => recordValue(TypeApply.copy(expr)(recordSubValues(x), ys), expr)
      case TypeApply(x, ys) => TypeApply.copy(expr)(recordSubValues(runtime, x), ys)
      case Select(x, y)     => Select.copy(expr)(recordAllValues(runtime, x), y)
      case Typed(x, tpe)    => Typed.copy(expr)(recordSubValues(runtime, x), tpe)
      case Repeated(xs, y)  => Repeated.copy(expr)(xs.map(recordAllValues(runtime, _)), y)
      case _                => expr
    }

  private[this] def recordValue(runtime: Term, expr: Term, origExpr: Term): Term = {
    // debug
    // println("recording " + expr.showExtractors + " at " + getAnchor(expr))
    val recordValueSel: Term = {
      val m = runtimeSym.method("recordValue").head
      runtime.select(m)
    }
    def skipIdent(sym: Symbol): Boolean =
      sym.fullName match {
        case "scala" | "java" => true
        case fullName if fullName.startsWith("scala.") => true
        case fullName if fullName.startsWith("java.")  => true
        case _ => false
      }

    def skipSelect(sym: Symbol): Boolean = {
      (sym match {
        case sym if sym.isDefDef => sym.signature.paramSigs.nonEmpty
        case sym if sym.isValDef => skipIdent(sym)
        case _ => true
      })

    }
    expr match {
      case Select(_, _) if skipSelect(expr.symbol) => expr
      case TypeApply(_, _) => expr
      case Ident(_) if skipIdent(expr.symbol) => expr
      case _ =>
        val tapply = recordValueSel.appliedToType(expr.tpe)
        Apply.copy(expr)(
          tapply,
          List(
            expr,
            Literal(Constant.Int(getAnchor(expr)))
          )
        )
    }
  }

  private[this] def getSourceCode(expr: Tree): String = {
    val pos = expr.pos
    (" " * pos.startColumn) + pos.sourceCode
  }

  private[this] def getAnchor(expr: Term): Int =
    expr match {
      case Apply(x, ys) if x.symbol.fullName == "verify.asserts.RecorderRuntime.recordValue" && ys.nonEmpty =>
        getAnchor(ys.head)
      case Apply(x, ys)     => getAnchor(x) + 0
      case TypeApply(x, ys) => getAnchor(x) + 0
      case Select(x, y)     =>
        expr.pos.startColumn + math.max(0, expr.pos.sourceCode.indexOf(y))
      case _                => expr.pos.startColumn
    }
}

object RecorderMacro {
  def apply[A: Type, R: Type](
      recording: Expr[A],
      listener: Expr[RecorderListener[A, R]])(using qctx: Quotes): Expr[R] =
    new RecorderMacro().apply(recording, '{""}, listener)

  /** captures a method invocation in the shape of assert(expr, message). */
  def apply[A: Type, R: Type](
      recording: Expr[A],
      message: Expr[String],
      listener: Expr[RecorderListener[A, R]])(using qctx: Quotes): Expr[R] =
    new RecorderMacro().apply(recording, message, listener)
}

object StringRecorderMacro {
  /** captures a method invocation in the shape of assertEquals(expected, found). */
  def apply[R: Type](
      expected: Expr[String],
      found: Expr[String],
      listener: Expr[RecorderListener[String, R]])(using qctx: Quotes): Expr[R] =
    new RecorderMacro().apply2[String, R](expected, found, '{""}, listener)

  /** captures a method invocation in the shape of assertEquals(expected, found). */
  def apply[R: Type](
      expected: Expr[String],
      found: Expr[String],
      message: Expr[String],
      listener: Expr[RecorderListener[String, R]])(using qctx: Quotes): Expr[R] =
    new RecorderMacro().apply2[String, R](expected, found, message, listener)
}
