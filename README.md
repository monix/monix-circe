# Streaming JSON parsing for circe with Monix Observable [![Build Status](https://travis-ci.org/Avasil/monix-circe.svg?branch=master)](https://travis-ci.org/Avasil/monix-circe) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.monix/monix-circe_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.monix/monix-circe_2.12)

## Quick Start

To use monix-circe in an existing SBT project with Scala 2.11 or a later version, add the following dependencies to your
`build.sbt` depending on your needs:

```scala
libraryDependencies ++= Seq(
  "io.monix" %% "monix-circe" % "0.0.1"
)
```

## Parsing

Monix-Circe provides different pipes to parse your streams of JSONs depending on whether your `Observable`
is:

- a \n-separated stream of JSON values or value stream:

```json
{ "repo": "monix-circe", "stars": 14 }
{ "repo": "monix-config", "stars": 5 }
```

- or a JSON array:

```json
[
  { "repo": "monix-circe", "stars": 14 },
  { "repo": "monix-config", "stars": 5 }
]
```

The appropriate `Operator` for the job also depends on your input stream value type (i.e. `String` or `Byte`).

The following table sums up every `Operator` available as a function of the input stream value type as
well as the JSON structure:

|                |String              |Byte              |
|----------------|--------------------|------------------|
|__Value stream__|`stringStreamParser`|`byteStreamParser`|
|__Array__       |`stringArrayParser` |`byteArrayParser` |

As an example, let's say we have a stream of strings representing a JSON array, we'll
pick the `stringArrayParser` pipe which converts a stream of `String` to a stream of `Json`, Circe's
representation of JSONs:

```scala
import io.circe.Json
import monix.circe._
import monix.reactive.Observable

val stringStream: Observable[String] = ???
val parsedStream: Observable[Json] = stringStream.liftByOperator(stringArrayParser)
```

## Decoding

Monix-Circe also comes with a `decoder` function which, given a `Decoder[A]`, produces a
`Observable[Json] => Observable[A]`.

For example, using Circe's fully automatic derivation:

```scala
import io.circe.generic.auto._
case class Foo(a: Int, b: String)
val parsedStream: Observable[Json] = ???
val decodedStream: Observable[Foo] = parsedStream.liftByOperator(decoder[Foo])
```

## Acknowledgements

Heavily inspired/based on [circe-fs2](https://github.com/circe/circe-fs2) and [circe-iteratee](https://github.com/circe/circe-iteratee).

## License

All code in this repository is licensed under the Apache License,
Version 2.0.  See [LICENSE.txt](./LICENSE).
