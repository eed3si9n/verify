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

import scala.scalanative.testinterface.PreloadedClassLoader

package object platform {
  type EnableReflectiveInstantiation =
    scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

  private[verify] def loadModule(name: String, loader: ClassLoader): Any =
    loader.asInstanceOf[PreloadedClassLoader].loadPreloaded(name)
}
