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

package monix

import io.circe.jawn.CirceSupportParser
import io.circe.{Decoder, Json}
import monix.reactive.Observable
import org.typelevel.jawn.{AsyncParser, ParseException}
import scala.collection.Seq

package object circe {

  /**
    * String parser of JSON values wrapped in a single array
    */
  final def stringArrayParser: Observable.Operator[String, Json] =
    stringParser(AsyncParser.UnwrapArray)

  /**
    * String parser of JSON values delimited by whitespace/newline
    */
  final def stringStreamParser: Observable.Operator[String, Json] =
    stringParser(AsyncParser.ValueStream)

  /**
    * Byte array parser of JSON values wrapped in a single array
    */
  final def byteArrayParser: Observable.Operator[Array[Byte], Json] =
    byteParser(AsyncParser.UnwrapArray)

  /**
    * Byte array parser of JSON values delimited by whitespace/newline
    */
  final def byteStreamParser: Observable.Operator[Array[Byte], Json] =
    byteParser(AsyncParser.ValueStream)

  final def byteParser(mode: AsyncParser.Mode): Observable.Operator[Array[Byte], Json] =
    new ParsingOperator[Array[Byte]](mode) {
      override final def parseWith(p: AsyncParser[Json])(in: Array[Byte]): Either[ParseException, Seq[Json]] =
        p.absorb(in)(CirceSupportParser.facade)
    }

  final def stringParser(mode: AsyncParser.Mode): Observable.Operator[String, Json] =
    new ParsingOperator[String](mode) {
      override final def parseWith(p: AsyncParser[Json])(in: String): Either[ParseException, Seq[Json]] =
        p.absorb(in)(CirceSupportParser.facade)
    }

  final def decoder[A](o: Observable[Json])(implicit decode: Decoder[A]): Observable[A] =
    o.flatMap { json =>
      decode(json.hcursor) match {
        case Left(df) => Observable.raiseError(df)
        case Right(a) => Observable.now(a)
      }
    }
}
