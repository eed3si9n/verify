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

/**
 * The `platform` package provides the required Scala types for
 * source-level compatibility between JVM/JS and Native, along with
 * utilities with a platform-specific implementation.
 */
package object platform {
  val Await = scala.concurrent.Await

  type EnableReflectiveInstantiation = verify.internal.EnableReflectiveInstantiation

  private[verify] def loadModule(name: String, loader: ClassLoader): Any = {
    verify.internal.Reflect.loadModule(name, loader)
  }
}
