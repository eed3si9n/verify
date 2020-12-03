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
package sourcecode

import language.experimental.macros
import scala.quoted._

abstract class NameMacros {
  inline given Name = ${Macros.nameImpl}
}

abstract class NameMachineMacros {
  inline given Name.Machine = ${Macros.nameMachineImpl}
}

abstract class FullNameMacros {
  inline given FullName = ${Macros.fullNameImpl}
}

abstract class FullNameMachineMacros {
  inline given FullName.Machine = ${Macros.fullNameMachineImpl}
}

abstract class SourceFilePathMacros {
  inline given SourceFilePath = ${Macros.sourceFilePathImpl}
}

abstract class SourceFileNameMacros {
  inline given SourceFileName = ${Macros.sourceFileNameImpl}
}

abstract class LineMacros {
  inline given sourcecode.Line = ${Macros.lineImpl}
}

abstract class EnclosingMacros {
  inline given Enclosing = ${Macros.enclosingImpl}
}

abstract class EnclosingMachineMacros {
  inline given Enclosing.Machine = ${Macros.enclosingMachineImpl}
}

abstract class PkgMacros {
  inline given Pkg = ${Macros.pkgImpl}
}

abstract class TextMacros {
  // given [T]: Conversion[T, Text[T]] {
  //   inline def apply(v: T): Text[T] = ${Macros.text[T]('v)}
  // }
  import scala.language.implicitConversions
  inline implicit def toScalaVerifySourcecodeText[T](v: T): Text[T] = ${Macros.text[T]('v)}
  inline def apply[T](v: T): Text[T] = ${Macros.text[T]('v)}
}

/*
abstract class ArgsMacros {
  inline given: Args = ${Macros.argsImpl}
}
*/

object Util{
  def isMacro(qctx: Quotes)(s: qctx.reflect.Symbol): Boolean = isMacroName(getName(qctx)(s))
  def isSynthetic(qctx: Quotes)(s: qctx.reflect.Symbol): Boolean = isSyntheticName(getName(qctx)(s))
  def isSyntheticName(name: String) = {
    name == "<init>" || (name.startsWith("<local ") && name.endsWith(">")) || isMacroName(name)
  }
  def isMacroName(name: String) = name.startsWith("macro")
  def getName(qctx: Quotes)(s: qctx.reflect.Symbol): String = {
    import qctx.reflect._
    // https://github.com/lampepfl/dotty/blob/0.20.0-RC1/library/src/scala/tasty/reflect/SymbolOps.scala
    s.name.trim
  }
  def cleanName(name0: String): String = {
    name0 match {
      case name if name.endsWith("$")    => cleanName(name.dropRight(1))
      case name if name.startsWith("_$") => cleanName(name.drop(2))
      case _ => name0
    }
  }
  def literal(qctx: Quotes)(value: String): Expr[String] = {
    import qctx.reflect._
    Literal(Constant.String(value)).asExpr.asInstanceOf[Expr[String]]
  }
  def literal(qctx: Quotes)(value: Int): Expr[Int] = {
    import qctx.reflect._
    Literal(Constant.Int(value)).asExpr.asInstanceOf[Expr[Int]]
  }
}

object Macros {

  def nameImpl(using qctx: Quotes): Expr[Name] = {
    import qctx.reflect._
    var owner = Symbol.spliceOwner
    while(Util.isSynthetic(qctx)(owner)) {
      owner = owner.owner
    }
    val simpleName = Util.cleanName(Util.getName(qctx)(owner))
    '{ Name(${Util.literal(qctx)(simpleName)}) }
  }

  def nameMachineImpl(using qctx: Quotes): Expr[Name.Machine] = {
    import qctx.reflect._
    var owner = Symbol.spliceOwner
    val simpleName = Util.getName(qctx)(owner)
    '{ Name.Machine(${Util.literal(qctx)(simpleName)}) }
  }

  def fullNameImpl(using qctx: Quotes): Expr[FullName] = {
    import qctx.reflect._
    var owner = Symbol.spliceOwner
    while(Util.isMacro(qctx)(owner)) {
      owner = owner.owner
    }
    val fullName =
      owner.fullName.trim
        .split("\\.", -1)
        .filterNot(Util.isSyntheticName)
        .map(Util.cleanName)
        .mkString(".")
    '{ FullName(${Util.literal(qctx)(fullName)}) }
  }

  def fullNameMachineImpl(using qctx: Quotes): Expr[FullName.Machine] = {
    import qctx.reflect._
    var owner = Symbol.spliceOwner
    val fullName = owner.fullName.trim
    '{ FullName.Machine(${Util.literal(qctx)(fullName)}) }
  }

  def sourceFileNameImpl(using qctx: Quotes): Expr[SourceFileName] = {
    import qctx.reflect._

    val name = SourceFile.current.jpath.getFileName.toString
    '{ SourceFileName(${Util.literal(qctx)(name)}) }
  }

  def sourceFilePathImpl(using qctx: Quotes): Expr[SourceFilePath] = {
    import qctx.reflect._
    val path = SourceFile.current.jpath.toString
    '{ SourceFilePath(${Util.literal(qctx)(path)}) }
  }

  def lineImpl(using qctx: Quotes): Expr[Line] = {
    import qctx.reflect._
    val line = Position.ofMacroExpansion.startLine + 1
    '{ Line(${Util.literal(qctx)(line)}) }
  }

  def enclosingImpl(using qctx: Quotes): Expr[Enclosing] = {
    val path = enclosing(qctx)(!Util.isSynthetic(qctx)(_))
    '{ Enclosing(${Util.literal(qctx)(path)}) }
  }

  def enclosingMachineImpl(using qctx: Quotes): Expr[Enclosing.Machine] = {
    val path = enclosing(qctx)(_ => true)
    '{ Enclosing.Machine(${Util.literal(qctx)(path)}) }
  }

  def pkgImpl(using qctx: Quotes): Expr[Pkg] = {
    import qctx.reflect._
    // https://github.com/lampepfl/dotty/blob/0.20.0-RC1/library/src/scala/tasty/reflect/SymbolOps.scala
    val path = enclosing(qctx)(_ match {
      case sym if sym.isPackageDef => true
      case _                       => false
    })
    '{ Pkg(${Util.literal(qctx)(path)}) }
  }

  /*
  def argsImpl(using qctx: Quotes): Expr[Args] = {
    import qctx.reflect._
    // import quoted._

    def nearestEnclosingMethod(owner: Symbol): Symbol =
      owner match {
        case IsDefDefSymbol(x)   => x
        case IsClassDefSymbol(x) => x
        case _                   => nearestEnclosingMethod(owner.owner)
      }
    def enclosingParamList: Seq[Seq[Symbol]] = {
      nearestEnclosingMethod(rootContext.owner) match {
        case IsDefDefSymbol(x) =>
          x.tree.paramss map { _ map {
            _.symbol
          }}
        case IsClassDefSymbol(x) =>
          x.tree.constructor.paramss map { _ map {
            _.symbol
          }}
      }
    }
    val param = enclosingParamList
    val texts: Seq[Seq[Expr[Text[_]]]] = param.map(_.map(p =>
      // this causes compiler to crash
      // '{ sourcecode.Text(${Ref(p).seal}, ${Util.literal(qctx)(p.name.toString)}) }
      '{ sourcecode.Text("?", ${Util.literal(qctx)(p.name.toString)}) }
    ))
    val textSeqs: Seq[Expr[Seq[Text[_]]]] = texts map { seq: Seq[Expr[Text[_]]] =>
      '{ Seq(${ Repeated(seq.map(_.unseal).toList, '[Text[_]].unseal).seal.asInstanceOf[Expr[Seq[Text[_]]]] }: _*) }
    }
    val seqss = '{ Seq(${ Repeated(textSeqs.map(_.unseal).toList, '[Seq[Text[_]]].unseal).seal.asInstanceOf[Expr[Seq[Seq[Text[_]]]]] }: _*) }
    '{ Args($seqss) }
  }
  */

  def text[T: Type](v: Expr[T])(using qctx: Quotes): Expr[sourcecode.Text[T]] = {
    import qctx.reflect._
    '{ Text($v, ${Util.literal(qctx)(Position.ofMacroExpansion.sourceCode.get)}) }
  }

  enum Chunk {
    case Pkg(name: String)
    case Obj(name: String)
    case Cls(name: String)
    case Trt(name: String)
    case Val(name: String)
    case Var(name: String)
    case Lzy(name: String)
    case Def(name: String)
  }

  def enclosing(qctx: Quotes)(filter: qctx.reflect.Symbol => Boolean): String = {
    import qctx.reflect._
    var current = Symbol.spliceOwner
    var path = List.empty[Chunk]

    while(current != Symbol.noSymbol && current.toString != "package <root>" && current.toString != "module class <root>"){
      if (filter(current)) {
        // https://github.com/lampepfl/dotty/blob/0.20.0-RC1/library/src/scala/tasty/reflect/SymbolOps.scala
        val chunk: String => Chunk = current match {
          case x if x.isPackageDef => Chunk.Pkg(_)
          case x if x.isClassDef && x.flags.is(Flags.Module) => Chunk.Obj(_)
          case x if x.isClassDef && x.flags.is(Flags.Trait) => Chunk.Trt(_)
          case x if x.isClassDef => Chunk.Cls(_)
          case x if x.isDefDef => Chunk.Def(_)
          case x if x.isValDef => Chunk.Val(_)
        }

        path = chunk(Util.getName(qctx)(current)) :: path
      }
      current = current.owner
    }
    val renderedPath: String = path.map{
      case Chunk.Pkg(s) => s + "."
      case Chunk.Obj(s) => Util.cleanName(s) + "."
      case Chunk.Cls(s) => s + "#"
      case Chunk.Trt(s) => s + "#"
      case Chunk.Val(s) => s + " "
      case Chunk.Var(s) => s + " "
      case Chunk.Lzy(s) => s + " "
      case Chunk.Def(s) => s + " "
    }.mkString.dropRight(1)
    renderedPath
  }

}
