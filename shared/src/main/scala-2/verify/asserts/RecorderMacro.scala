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

import scala.reflect.macros.blackbox.Context
import scala.util.Properties

class RecorderMacro[C <: Context](val context: C) {
  import context.universe._

  def apply[A: context.WeakTypeTag, R: context.WeakTypeTag](value: context.Tree, message: context.Tree): Expr[R] = {
    context.Expr(
      Block(
        declareRuntime[A, R] ::
          recordMessage(message) ::
          recordExpressions(value),
        completeRecording
      )
    )
  }

  private[this] def termName(c: C)(s: String) =
    c.universe.TermName(s)

  private[this] def declareRuntime[A: context.WeakTypeTag, R: context.WeakTypeTag]: Tree = {
    val runtimeClass = context.mirror.staticClass(classOf[RecorderRuntime[_, _]].getName())
    ValDef(
      Modifiers(),
      termName(context)("$scala_verify_recorderRuntime"),
      TypeTree(weakTypeOf[RecorderRuntime[A, R]]),
      Apply(
        Select(New(Ident(runtimeClass)), termNames.CONSTRUCTOR),
        List(Select(context.prefix.tree, termName(context)("listener")))
      )
    )
  }

  private[this] def recordExpressions(recording: Tree): List[Tree] = {
    val source = getSourceCode(recording)
    val ast = showRaw(recording)
    try {
      List(resetValues, recordExpression(source, ast, recording))
    } catch {
      case e: Throwable =>
        throw new RuntimeException("Expecty: Error rewriting expression.\nText: " + source + "\nAST : " + ast, e)
    }
  }

  private[this] def recordMessage(message: Tree): Tree =
    Apply(
      Select(Ident(termName(context)("$scala_verify_recorderRuntime")), termName(context)("recordMessage")),
      List(message)
    )

  private[this] def completeRecording: Tree =
    Apply(
      Select(Ident(termName(context)("$scala_verify_recorderRuntime")), termName(context)("completeRecording")),
      List()
    )

  private[this] def resetValues: Tree =
    Apply(
      Select(Ident(termName(context)("$scala_verify_recorderRuntime")), termName(context)("resetValues")),
      List()
    )

  private[this] def recordExpression(source: String, ast: String, expr: Tree) = {
    val instrumented = recordAllValues(expr)
    log(expr, s"""
Expression      : ${source.trim()}
Original AST    : $ast
Instrumented AST: ${showRaw(instrumented)}")

    """)
    Apply(
      Select(Ident(termName(context)("$scala_verify_recorderRuntime")), termName(context)("recordExpression")),
      List(context.literal(source).tree, context.literal(ast).tree, instrumented)
    )
  }

  private[this] def recordAllValues(expr: Tree): Tree = expr match {
    case New(_)     => expr // only record after ctor call
    case Literal(_) => expr // don't record
    // don't record value of implicit "this" added by compiler; couldn't find a better way to detect implicit "this" than via point
    case Select(x @ This(_), y) if getPosition(expr).point == getPosition(x).point => expr
    case x: Select if x.symbol.isModule                                            => expr // don't try to record the value of packages
    case _                                                                         => recordValue(recordSubValues(expr), expr)
  }

  private[this] def recordSubValues(expr: Tree): Tree = expr match {
    case Apply(x, ys)     => Apply(recordAllValues(x), ys.map(recordAllValues))
    case TypeApply(x, ys) => recordValue(TypeApply(recordSubValues(x), ys), expr)
    case Select(x, y)     => Select(recordAllValues(x), y)
    case _                => expr
  }

  private[this] def recordValue(expr: Tree, origExpr: Tree): Tree =
    if (origExpr.tpe.typeSymbol.isType)
      Apply(
        Select(Ident(termName(context)("$scala_verify_recorderRuntime")), termName(context)("recordValue")),
        List(expr, Literal(Constant(getAnchor(origExpr))))
      )
    else expr

  private[this] def getSourceCode(expr: Tree): String = getPosition(expr).lineContent

  private[this] def getAnchor(expr: Tree): Int = expr match {
    case Apply(x, ys)     => getAnchor(x) + 0
    case TypeApply(x, ys) => getAnchor(x) + 0
    case _ => {
      val pos = getPosition(expr)
      pos.point - pos.source.lineToOffset(pos.line - 1)
    }
  }

  private[this] def getPosition(expr: Tree) = expr.pos.asInstanceOf[scala.reflect.internal.util.Position]

  private[this] def log(expr: Tree, msg: => String): Unit = {
    if (Properties.propOrFalse("scala.verify.debug")) context.info(expr.pos, msg, force = false)
  }
}

object RecorderMacro1 {
  def apply[A: context.WeakTypeTag, R: context.WeakTypeTag](
      context: Context
  )(value: context.Tree): context.Expr[R] = {
    new RecorderMacro[context.type](context).apply[A, R](value, context.literal("").tree)
  }
}

object RecorderMacro {
  def apply[A: context.WeakTypeTag, R: context.WeakTypeTag](
      context: Context
  )(value: context.Tree, message: context.Tree): context.Expr[R] = {
    new RecorderMacro[context.type](context).apply[A, R](value, message)
  }
}
