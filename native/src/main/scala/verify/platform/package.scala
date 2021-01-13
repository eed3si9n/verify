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
import org.scalajs.testinterface.TestUtils
import scala.concurrent.ExecutionContext
import scala.util.Try

package object platform {
  val defaultExecutionContext: ExecutionContext = ExecutionContext.global

  // Deprecated in 0.4.0, required for 0.3.9 support
  @silent("deprecated") 
  type EnableReflectiveInstantiation =
    scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

  // Deprecated in 0.4.0, required for 0.3.9 support
  // Unfortunately, the TestUtils semantics changed from 0.3.9 to 0.4.0, so we need to try both names
  @silent("deprecated") 
  private[verify] def loadModule(name: String, loader: ClassLoader): Any =
    Try(TestUtils.loadModule(name, loader)).getOrElse(TestUtils.loadModule(name + "$", loader))
    
}
