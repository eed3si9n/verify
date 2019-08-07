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

package cutest.internal

import java.lang.reflect.Modifier

object Reflect {
  def loadModule(name: String, loader: ClassLoader): Any = {
    load(name + "$", loader).filter(isModuleClass) match {
      case Some(runtimeClass) =>
        try {
          runtimeClass.getField("MODULE$").get(null)
        } catch {
          case e: java.lang.ExceptionInInitializerError =>
            val cause = e.getCause
            if (cause == null) throw e
            else throw cause
        }
      case _ => throw new ClassNotFoundException(name)
    }
  }

  private def isModuleClass(clazz: Class[_]): Boolean = {
    try {
      val fld = clazz.getField("MODULE$")
      clazz.getName.endsWith("$") && (fld.getModifiers & Modifier.STATIC) != 0
    } catch {
      case _: NoSuchFieldException => false
    }
  }

  private def load(fqcn: String, loader: ClassLoader): Option[Class[_]] = {
    try {
      /* initialize = false, so that the constructor of a module class is not
       * executed right away. It will only be executed when we call
       * `loadModule`.
       */
      val clazz = Class.forName(fqcn, false, loader)
      Some(clazz)
    } catch {
      case _: ClassNotFoundException => None
    }
  }
}
