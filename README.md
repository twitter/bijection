## Bijection [![Build Status](https://secure.travis-ci.org/twitter/bijection.png)](http://travis-ci.org/twitter/bijection)

A Bijection is an invertible function that converts back and forth between two different types, with the contract that a round-trip through the Bijection will bring back the original object.

Many Bijections are supplied by default. Use the `Bijection` object to apply Bijections that are present implicitly:

```scala
import com.twitter.bijection._

scala> Bijection[Int, Array[Byte]](100)
res1: Array[Byte] = Array(0, 0, 0, 100)

scala> Bijection[Int, String](100)
res2: String = 100

scala> Bijection[Int, java.lang.Integer](100)
res3: java.lang.Integer = 100
```

Use `invert` to reverse the transformation:

```scala
scala> Bijection.invert[Int, String](res2)
res4: Int = 100
```

Bijections can also be composed. As with functions, `andThen` composes forward, `compose` composes backward.

This example round-trips a long into a GZipped base64-encoded string:

```scala
scala> val bijection = Bijection.long2BigEndian andThen Bijection.bytes2GZippedBase64
bijection: com.twitter.bijection.Bijection[Long,com.twitter.bijection.GZippedBase64String] = <function1>

scala> bijection(123456789L)
res8: com.twitter.bijection.GZippedBase64String = GZippedBase64String(H4sIAAAAAAAAAGNgYGBgjz4rCgBpa5WLCAAAAA==)

scala> bijection.invert(res8)
res9: Long = 123456789
```

## Supported Bijections

Bijection implicitly supplies Bijections between:

* all numeric types <-> their boxed java counterparts
* all numeric types <-> big-endian `Array[Byte]` encodings
* all numeric types <-> String
* Bijections for all `asScala`, `asJava` pairs provided by [scala.collection.JavaConverters](http://www.scala-lang.org/api/current/scala/collection/JavaConverters$.html)
* String <-> utf8 encoded bytes
* `Array[Byte]` <-> `GZippedBytes`
* `Array[Byte]` <-> `Base64String`
* `Array[Byte]` <-> `GZippedBase64String`
* `Array[Byte]` <-> `java.nio.ByteBuffer`
* `Class[T]` <-> String
* `A => B` <-> `C => D` (function conversion)
* Bijection builders for all tuples. (`(String,Int)` <-> `(Array[Byte], java.lang.Integer)` is built automatically, for example.)

## Maven

Current version is `0.1.0`. groupid=`"com.twitter"` artifact=`"bijection_2.9.2"`.

## Authors

* Oscar Boykin <http://twitter.com/posco>
* Marius Eriksen <http://twitter.com/marius>
* Sam Ritchie <http://twitter.com/sritchie>

## License

Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
