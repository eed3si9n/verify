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

import scala.annotation.StaticAnnotation

// This is a dummy annotation created to match
// https://github.com/portable-scala/portable-scala-reflect/blob/v0.1.0/jvm/src/main/java/org/portablescala/reflect/annotation/EnableReflectiveInstantiation.java
// but this is acutally not used.
final class EnableReflectiveInstantiation extends StaticAnnotation
