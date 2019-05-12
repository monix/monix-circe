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

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json, ParsingFailure}
import minitest.TestSuite
import minitest.laws.Checkers
import monix.execution.Scheduler
import monix.reactive.Observable
import org.typelevel.jawn.AsyncParser

trait BaseSuite extends TestSuite[Scheduler] with Checkers with TestCases {

  def setup(): Scheduler = Scheduler.global

  def tearDown(env: Scheduler): Unit = {
    ()
  }

  def testParser(mode: AsyncParser.Mode, through: Observable[String] => Observable[Json])(
    implicit s: Scheduler): Unit = {
    check2 { (fooStream: Stream[Foo], fooVector: Vector[Foo]) =>
      val observable: Observable[Json] =
        through(serializeFoos(mode, fooObservable(fooStream.take(1), fooVector.take(1))))
      val foos = (fooStream.take(1) ++ fooVector.take(1)).map(_.asJson).toList

      observable.toListL.attempt.runSyncUnsafe() == Right(foos)
    }
  }

  def testParsingFailure(through: Observable[String] => Observable[Json])(implicit s: Scheduler): Unit = {
    check2 { (stringStream: Stream[String], stringVector: Vector[String]) =>
      val result =
        through(Observable.now("}") ++ stringObservable(stringStream, stringVector)).toListL.attempt.runSyncUnsafe()
      result.isLeft && result.left.get.isInstanceOf[ParsingFailure]
    }
  }

  implicit val decodeFoo: Decoder[Foo] = Foo.decodeFoo
  implicit val encodeFoo: Encoder[Foo] = Foo.encodeFoo

  def stringToBytes: Observable[String] => Observable[Array[Byte]] = _.map(
    _.getBytes(java.nio.charset.Charset.forName("UTF-8"))
  )

  def fooObservable(fooStream: Stream[Foo], fooVector: Vector[Foo]): Observable[Foo] =
    Observable.fromIterable(fooStream) ++ Observable.fromIterable(fooVector)

  def stringObservable(stringStdStream: Stream[String], stringVector: Vector[String]): Observable[String] =
    Observable.fromIterable(stringStdStream) ++ Observable.fromIterable(stringVector)

  def serializeFoos(parsingMode: AsyncParser.Mode, foos: Observable[Foo]): Observable[String] =
    parsingMode match {
      case AsyncParser.ValueStream | AsyncParser.SingleValue =>
        foos.map((_: Foo).asJson.spaces2).intersperse("\n")
      case AsyncParser.UnwrapArray =>
        Observable.now("[") ++ foos.map((_: Foo).asJson.spaces2).intersperse(", ") ++ Observable.now("]")
    }
}
