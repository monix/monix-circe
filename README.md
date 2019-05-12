# Streaming JSON parsing for circe with Monix Observable [![Build Status](https://travis-ci.com/Avasil/monix-circe.svg?branch=master)](https://travis-ci.com/Avasil/monix-circe) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.monix/monix-circe_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.monix/monix-circe_2.12)

## Quick Start

To use monix-circe in an existing SBT project with Scala 2.11 or a later version, add the following dependencies to your
`build.sbt` depending on your needs:

```scala
libraryDependencies ++= Seq(
  "io.monix" %% "monix-circe" % "<version>"
)
```

## Acknowledgements

Inspired by [circe-fs2](https://github.com/circe/circe-fs2) and [circe-iteratee](https://github.com/circe/circe-iteratee).

## License

All code in this repository is licensed under the Apache License,
Version 2.0.  See [LICENSE.txt](./LICENSE).