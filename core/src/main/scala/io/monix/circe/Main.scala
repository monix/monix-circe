package io.monix.circe

import cats.effect.ExitCode
import cats.syntax.functor._
import monix.eval.{Task, TaskApp}

object Main extends TaskApp {

  def run(args: List[String]): Task[ExitCode] = {
    Task(println("I am a new project!")).as(ExitCode.Success)
  }

}