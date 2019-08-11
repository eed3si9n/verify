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

package verify.runner

/**
 * Configurable options for the runner.
 *
 * @param useSbtLogging specifies whether to use SBT's test-logging infrastructure,
 *        or just println.
 *
 *        Defaults to `println` because SBT's test logging doesn't seem to give us
 *        anything that we want, and does annoying things like making a left-hand
 *        gutter and buffering input by default.
 *
 *        Option inspired by its availability in uTest and other testing frameworks.
 */
final case class Options(
    useSbtLogging: Boolean = false
)
