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

If you `import Bijection.asMethod` you can use `.as[T]` to do the default bijection to `T`:

```scala
scala> import com.twitter.bijection.Bijection.asMethod
import com.twitter.bijection.Bijection.asMethod

scala> 1.as[String]
res0: String = 1
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

When you have bijections between a path of items you can `Bijection.connect` them:

```scala
scala> import com.twitter.bijection.Bijection.{asMethod, connect}
import com.twitter.bijection.Bijection.{asMethod, connect}

scala> import com.twitter.bijection.Base64String
import com.twitter.bijection.Base64String

scala> implicit val string2Long2Bytes2B64 = connect[String,Long,Array[Byte],Base64String]
string2Long2Bytes2B64: com.twitter.bijection.Bijection[String,com.twitter.bijection.Base64String] = <function1>

scala> "243".as[Base64String]
res0: com.twitter.bijection.Base64String = Base64String(AAAAAAAAAPM=)

scala> res0.as[String]
res1: String = 243
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

Additionally there is a method to generate Bijections between most of Scala's built in types:
```Bijection.toContainer[Int,String,List[Int],Vector[String]``` returns
```Bijection[List[Int], Vector[String]```

If you see a reversible conversion that is not here and related to types in the standard library
of Java or Scala, please contribute!

## Maven

Current version is `0.1.0`. groupid=`"com.twitter"` artifact=`"bijection-core_2.9.2"`.

## Authors

* Oscar Boykin <http://twitter.com/posco>
* Marius Eriksen <http://twitter.com/marius>
* Sam Ritchie <http://twitter.com/sritchie>

## License

Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
