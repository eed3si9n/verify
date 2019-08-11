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

object Compat {
  type Context = scala.reflect.macros.blackbox.Context
  def enclosingOwner(c: Context) = c.internal.enclosingOwner

  def enclosingParamList(c: Context): List[List[c.Symbol]] = {
    def nearestEnclosingMethod(owner: c.Symbol): c.Symbol =
      if (owner.isMethod) owner
      else if (owner.isClass) owner.asClass.primaryConstructor
      else nearestEnclosingMethod(owner.owner)

    nearestEnclosingMethod(enclosingOwner(c)).asMethod.paramLists
  }

  def isDotty: Boolean = false
}
