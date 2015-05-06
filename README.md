## Bijection [![Build Status](https://secure.travis-ci.org/twitter/bijection.png)](http://travis-ci.org/twitter/bijection) [![Coverage Status](https://coveralls.io/repos/twitter/bijection/badge.png?branch=develop)](https://coveralls.io/r/twitter/bijection?branch=develop)

A Bijection is an invertible function that converts back and forth between two types, with
the contract that a round-trip through the Bijection will bring back the original object. Moreover,
the inverse has the same property.

See the [current API documentation](http://twitter.github.com/bijection) for more information.

## Examples:

```scala
> ./sbt bijection-core/console
scala> import com.twitter.bijection._
scala> Bijection[Int, java.lang.Integer](42)
res0: java.lang.Integer = 42
```

In addition to Bijection, we have Injection. An Injection embeds a type A in a larger space of type
B. Every item from A can be round-tripped through B, but not every B can be mapped to A. So
Injection is like a pair of function: `A => B, B => Try[A]`.

```scala
import com.twitter.bijection._

scala> Injection[Int, String](100)
res0: String = 100

scala> Injection.invert[Int, String](res0)
res1: Try[Int] = Success(100)
```
If we want to treat an Injection like a Bijection (over a restricted subspace of the larger set),
we use the `B @@ Rep[A]` syntax, for instance: `String @@ Rep[Int]`

```scala
Bijection[Int, String @@ Rep[Int]](100)
res2: com.twitter.bijection.package.@@[String,com.twitter.bijection.Rep[Int]] = 100
```

Use `invert` to reverse the transformation:

```scala
scala> Bijection.invert[Int, String @@ Rep[Int]](res2)
res3: Int = 100
```

If you `import Conversion.asMethod` you can use `.as[T]` to use an available Bijection/Injection to `T`:

```scala
scala> import com.twitter.bijection.Conversion.asMethod
import com.twitter.bijection.Conversion.asMethod

scala> 1.as[java.lang.Integer]
res6: java.lang.Integer = 1
```

Bijections and Injections can also be composed. As with functions, `andThen` composes forward, `compose` composes backward.

This example round-trips a long into a GZipped base64-encoded string:

```scala
scala> val injection = Injection.long2BigEndian andThen Bijection.bytes2GZippedBase64
injection: com.twitter.bijection.Injection[Long,Array[Byte]] = <function1>

scala> injection(123456789L)
res1: com.twitter.bijection.GZippedBase64String = GZippedBase64String(H4sIAAAAAAAAAGNgYGBgjz4rCgBpa5WLCAAAAA==)

scala> injection.invert(res1)
res2: Try[Long] = Success(123456789)
```

When you have bijections between a path of items you can `Bijection.connect` or `Injection.connect` them:

```scala
scala> import com.twitter.bijection.Injection.connect
import com.twitter.bijection.Injection.connect

scala> import com.twitter.bijection.Base64String
import com.twitter.bijection.Base64String

scala> import Conversion.asMethod
import Conversion.asMethod

scala> implicit val long2String2Bytes2B64 = connect[Long,String,Array[Byte],Base64String]
string2Long2Bytes2B64: com.twitter.bijection.Bijection[String,com.twitter.bijection.Base64String] = <function1>

scala> 243L.as[Base64String]
res0: com.twitter.bijection.Base64String = Base64String(MjQz)

scala> long2String2Bytes2B64.invert(res0)
res1: Try[Long] = Success(243)
```

## Supported Bijections/Injections

Bijection implicitly supplies Bijections between:

* all numeric types <-> their boxed java counterparts
* containers/primitives <-> Json (Injections via bijection-json)
* thrift/protobuf/avro <-> `Array[Byte]` (Injections via bijection-protobuf/bijection-thrift/bijection-avro)
* all numeric types <-> big-endian `Array[Byte]` encodings (Injections)
* all numeric types <-> String (Injections)
* Bijections for all `asScala`, `asJava` pairs provided by [scala.collection.JavaConverters](http://www.scala-lang.org/api/current/scala/collection/JavaConverters$.html)
* String <-> utf8 encoded bytes
* `Array[Byte]` <-> `GZippedBytes`
* `Array[Byte]` <-> `Base64String`
* `Array[Byte]` <-> `GZippedBase64String`
* `Array[Byte]` <-> `java.nio.ByteBuffer`
* `Class[T]` <-> String (Injection)
* `A => B` <-> `C => D` (function conversion)
* Bijection/Injection builders for all tuples. (`(String,Int)` <-> `(Array[Byte], java.lang.Integer)` is built automatically, for example.)

Additionally there is a method to generate Bijections between most of Scala's built in types:
```Bijection.toContainer[Int,String,List[Int],Vector[String]``` returns
```Bijection[List[Int], Vector[String]```

If you see a reversible conversion that is not here and related to types in the standard library
of Java or Scala, please contribute!

## Serialization via Bufferable

`Bufferable[T]` handles putting and getting a type `T` into a ByteBuffer in a composable way.
`Bufferable[T]` instances for all primitives/tuples/containers are provided. Bijections and
Injections to any of these types give you binary serialization via Bufferable.

## Community and Documentation

This, and all github.com/twitter projects, are under the [Twitter Open Source Code of Conduct](https://engineering.twitter.com/opensource/code-of-conduct). Additionally, see the [Typelevel Code of Conduct](http://typelevel.org/conduct) for specific examples of harassing behavior that are not tolerated.

To learn more and find links to tutorials and information around the web, check out the [Bijection Wiki](https://github.com/twitter/bijection/wiki).

The latest ScalaDocs are hosted on Bijection's [Github Project Page](http://twitter.github.io/bijection).

Discussion occurs primarily on the [Bijection mailing list](https://groups.google.com/forum/#!forum/bijection). Issues should be reported on the [GitHub issue tracker](https://github.com/twitter/bijection/issues).

## Maven

Bijection modules are available on maven central. The current groupid and version for all modules is, respectively, `"com.twitter"` and  `0.7.2`.

Current published artifacts are

* `bijection-core`
* `bijection-protobuf`
* `bijection-thrift`
* `bijection-guava`
* `bijection-scrooge`
* `bijection-json`
* `bijection-util`
* `bijection-clojure`
* `bijection-netty`
* `bijection-avro`
* `bijection-hbase`

Every artifact is published against Scala `"2.10"` and `"2.11"`. To pull in the jars, make sure to add your desired scala version as a suffix, ie:

`bijection-core_2.10` or `bijection-core_2.11`

## Authors

* Oscar Boykin <http://twitter.com/posco>
* Marius Eriksen <http://twitter.com/marius>
* Sam Ritchie <http://twitter.com/sritchie>

## License

Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
