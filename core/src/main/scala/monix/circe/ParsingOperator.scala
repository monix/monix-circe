/*
 * Copyright (c) 2019-2019 by The Monix Project Developers.
 * See the project homepage at: https://monix.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monix.circe
import io.circe.jawn.CirceSupportParser
import io.circe.{Json, ParsingFailure}
import monix.execution.Ack
import monix.execution.Ack.Stop
import monix.reactive.Observable
import monix.reactive.observers.Subscriber
import org.typelevel.jawn.{AsyncParser, ParseException}

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.collection.Seq

private[circe] abstract class ParsingOperator[In](mode: AsyncParser.Mode) extends Observable.Operator[In, Json] {

  protected[this] def parseWith(p: AsyncParser[Json])(in: In): Either[ParseException, Seq[Json]]

  private[this] def mkParser: AsyncParser[Json] = CirceSupportParser.async(mode = mode)

  def apply(out: Subscriber[Json]): Subscriber[In] = {
    new Subscriber[In] {
      implicit val scheduler = out.scheduler
      private[this] var isDone = false
      private[this] val parser = mkParser

      def onNext(elem: In): Future[Ack] = {
        try {
          parseWith(parser)(elem) match {
            case Left(error) =>
              onError(ParsingFailure(error.getMessage, error))
              Stop
            case Right(js) =>
              out.onNextAll(js)
          }
        } catch {
          case NonFatal(ex) =>
            onError(ex)
            Stop
        }
      }

      def onError(ex: Throwable): Unit =
        if (!isDone) {
          isDone = true
          out.onError(ex)
        }

      def onComplete(): Unit =
        if (!isDone) {
          isDone = true
          out.onComplete()
        }
    }
  }
}
