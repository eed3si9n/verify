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

import com.github.ghik.silencer.silent
import scala.scalanative.reflect.Reflect
import scala.concurrent.ExecutionContext

package object platform {
  val Await = scala.concurrent.Await
  val defaultExecutionContext: ExecutionContext = ExecutionContext.global

  @silent("deprecated")
  type EnableReflectiveInstantiation =
    scala.scalanative.reflect.annotation.EnableReflectiveInstantiation

  @silent("deprecated")
  private[verify] def loadModule(name: String, loader: ClassLoader): Any =
    (for {
      cls <- Reflect.lookupLoadableModuleClass(name)
    } yield cls.loadModule())
      .orElse(for {
        cls <- Reflect.lookupLoadableModuleClass(name + "$")
      } yield cls.loadModule())
      .get
}
