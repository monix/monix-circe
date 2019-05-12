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

import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import monix.reactive.Observable
import org.typelevel.jawn.AsyncParser

object StreamingSpec extends BaseSuite {

  test("stringArrayParser should parse values wrapped in array") { implicit s =>
    testParser(AsyncParser.UnwrapArray, _.liftByOperator(stringArrayParser))
  }

  test("byteArrayParser should parse bytes wrapped in array") { implicit s =>
    testParser(AsyncParser.UnwrapArray, stringToBytes.andThen(_.liftByOperator(byteArrayParser)))
  }

  test("stringStreamParser should parse values delimited by new lines") { implicit s =>
    testParser(AsyncParser.ValueStream, _.liftByOperator(stringStreamParser))
  }

  test("byteStreamParser should parse bytes delimited by new lines") { implicit s =>
    testParser(AsyncParser.ValueStream, stringToBytes.andThen(_.liftByOperator(byteStreamParser)))
  }

  test("decoder should decode enumerated JSON values") { implicit s =>
    check2 { (fooStream: Stream[Foo], fooVector: Vector[Foo]) =>
      val stream: Observable[String] = serializeFoos(AsyncParser.UnwrapArray, fooObservable(fooStream, fooVector))
      val foos: List[Foo] = (fooStream ++ fooVector).toList

      decoder[Foo](stream.liftByOperator(stringArrayParser)).toListL.attempt.runSyncUnsafe() == Right(foos)
    }
  }

  test("stringParser should parse single value") { implicit s =>
    check1 { foo: Foo =>
      val stream = serializeFoos(AsyncParser.SingleValue, Observable.now(foo))
      stream.liftByOperator(stringParser(AsyncParser.SingleValue)).toListL.attempt.runSyncUnsafe() == Right(
        List(foo.asJson))
    }
  }

  test("byteParser should parse single value") { implicit s =>
    check1 { foo: Foo =>
      val stream = serializeFoos(AsyncParser.SingleValue, Observable.now(foo))
      stringToBytes(stream)
        .liftByOperator(byteParser(AsyncParser.SingleValue))
        .toListL
        .attempt
        .runSyncUnsafe() == Right(List(foo.asJson))
    }
  }

  test("byteParser should parse single value, when run twice") { implicit s =>
    check1 { foo: Foo =>
      val stream = serializeFoos(AsyncParser.SingleValue, Observable.now(foo))

      val parseOnce =
        stringToBytes(stream).liftByOperator(byteParser(AsyncParser.SingleValue)).toListL

      parseOnce.attempt.runSyncUnsafe() == Right(List(foo.asJson))
    }
  }

  test("stringArrayParser should return ParsingFailure") { implicit s =>
    testParsingFailure(_.liftByOperator(stringArrayParser))
  }

  test("stringStreamParser should return ParsingFailure") { implicit s =>
    testParsingFailure(_.liftByOperator(stringStreamParser))
  }

  test("byteArrayParser should return ParsingFailure") { implicit s =>
    testParsingFailure(stringToBytes.andThen(_.liftByOperator(byteArrayParser)))
  }

  test("byteStreamParser should return ParsingFailure") { implicit s =>
    testParsingFailure(stringToBytes.andThen(_.liftByOperator(byteStreamParser)))
  }

  test("decoder should return DecodingFailure") { implicit s =>
    check2 { (fooStream: Stream[Foo], fooVector: Vector[Foo]) =>
      sealed trait Foo2
      case class Bar2(x: String) extends Foo2

      if (fooStream.nonEmpty && fooVector.nonEmpty) {
        val result = decoder[Foo2](
          serializeFoos(AsyncParser.UnwrapArray, fooObservable(fooStream, fooVector))
            .liftByOperator(stringArrayParser)).toListL.attempt
          .runSyncUnsafe()

        result.isLeft && result.left.get.isInstanceOf[DecodingFailure]
      } else true
    }
  }
}
