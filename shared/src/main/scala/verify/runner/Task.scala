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
package runner

import sbt.testing.{ Task => BaseTask, _ }
import scala.compat.Platform.EOL
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future, Promise }
import scala.util.Try
import verify.platform.loadModule

final class Task(task: TaskDef, opts: Options, cl: ClassLoader, execContext: ExecutionContext) extends BaseTask {
  private[this] implicit lazy val ec: ExecutionContext = execContext
  private[this] val console = if (opts.useSbtLogging) None else Some(Array(new ConsoleLogger))

  def tags(): Array[String] = Array.empty
  def taskDef(): TaskDef = task

  def reportStart(name: String, loggers: Array[Logger]): Unit = {
    for (logger <- console.getOrElse(loggers)) {
      val withColors = logger.ansiCodesSupported()
      val color = if (withColors) Console.GREEN else ""
      val reset = if (withColors) Console.RESET else ""
      logger.info(color + name + reset + EOL)
    }
  }

  def report(name: String, r: Result[_], loggers: Array[Logger]): Unit = {
    for (logger <- console.getOrElse(loggers)) {
      logger.info(r.formatted(name, logger.ansiCodesSupported()))
    }
  }

  def execute(eventHandler: EventHandler, loggers: Array[Logger], continuation: Array[BaseTask] => Unit): Unit = {

    def loop(props: Iterator[TestSpec[Unit, Unit]]): Future[Unit] = {
      if (!props.hasNext) unit
      else {
        val property = props.next()
        val startTS = System.currentTimeMillis()
        val futureResult = property(())

        futureResult.flatMap { result =>
          val endTS = System.currentTimeMillis()

          report(property.name, result, loggers)
          eventHandler.handle(event(result, endTS - startTS))
          loop(props)
        }
      }
    }

    val future = loadSuite(task.fullyQualifiedName(), cl).fold(unit) { suite =>
      reportStart(task.fullyQualifiedName(), loggers)
      suite.properties.setupSuite()
      loop(suite.properties.iterator).map { _ =>
        suite.properties.tearDownSuite()
      }
    }

    future.onComplete(_ => continuation(Array.empty))
  }

  def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[BaseTask] = {
    val p = Promise[Unit]()
    execute(eventHandler, loggers, _ => p.success(()))
    Await.result(p.future, Duration.Inf)
    Array.empty
  }

  def loadSuite(name: String, loader: ClassLoader): Option[AbstractTestSuite] = {
    Try(loadModule(name, loader)).toOption
      .collect { case ref: AbstractTestSuite => ref }
  }

  def event(result: Result[Unit], durationMillis: Long): Event = new Event {
    def fullyQualifiedName(): String =
      task.fullyQualifiedName()

    def throwable(): OptionalThrowable =
      result match {
        case Result.Exception(source, _) =>
          new OptionalThrowable(source)
        case Result.Failure(_, Some(source), _) =>
          new OptionalThrowable(source)
        case _ =>
          new OptionalThrowable()
      }

    def status(): Status =
      result match {
        case Result.Exception(_, _) =>
          Status.Error
        case Result.Failure(_, _, _) =>
          Status.Failure
        case Result.Success(_) =>
          Status.Success
        case Result.Ignored(_, _) =>
          Status.Ignored
        case Result.Canceled(_, _) =>
          Status.Canceled
      }

    def selector(): Selector = {
      task.selectors().head
    }

    def fingerprint(): Fingerprint =
      task.fingerprint()

    def duration(): Long =
      durationMillis
  }

  private[this] val unit = Future.successful(())
}
