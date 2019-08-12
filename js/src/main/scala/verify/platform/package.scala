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

import org.portablescala.reflect.Reflect
import scala.concurrent.ExecutionContext

/**
 * The `platform` package provides the required Scala types for
 * source-level compatibility between JVM/JS and Native, along with
 * utilities with a platform-specific implementation.
 */
package object platform {
  val Await = scala.concurrent.Await
  val defaultExecutionContext: ExecutionContext = ExecutionContext.global

  type EnableReflectiveInstantiation =
    org.portablescala.reflect.annotation.EnableReflectiveInstantiation

  private[verify] def loadModule(name: String, loader: ClassLoader): Any = {
    Reflect
      .lookupLoadableModuleClass(name + "$", loader)
      .getOrElse(throw new ClassNotFoundException(name))
      .loadModule()
  }
}
