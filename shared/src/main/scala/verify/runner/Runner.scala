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

import sbt.testing.{ Runner => BaseRunner, Task => BaseTask, _ }
import scala.concurrent.ExecutionContext

final class Runner(
    val args: Array[String],
    val remoteArgs: Array[String],
    val options: Options,
    val ec: ExecutionContext,
    classLoader: ClassLoader
) extends BaseRunner {

  def done(): String = ""

  def tasks(list: Array[TaskDef]): Array[BaseTask] = {
    list.map(t => new Task(t, options, classLoader, ec))
  }

  def receiveMessage(msg: String): Option[String] = {
    None
  }

  def serializeTask(task: BaseTask, serializer: TaskDef => String): String =
    serializer(task.taskDef())

  def deserializeTask(task: String, deserializer: String => TaskDef): BaseTask =
    new Task(deserializer(task), options, classLoader, ec)
}
