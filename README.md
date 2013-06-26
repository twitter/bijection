## Bijection [![Build Status](https://secure.travis-ci.org/twitter/bijection.png)](http://travis-ci.org/twitter/bijection)

A Bijection is an invertible function that converts back and forth between two types, with
the contract that a round-trip through the Bijection will bring back the original object. Moreover,
the inverse has the same property.

See the [current API documentation](http://twitter.github.com/bijection) for more information.

## Maven

Current version is `0.4.0`. groupid=`"com.twitter"` artifact=`"bijection-core_2.9.3"`.

## Examples:

```scala
scala> Bijection[Int, java.lang.Integer](42)
res0: java.lang.Integer = 42
```

In addition to Bijection, we have Injection. An Injection embeds a type A in a larger space of type
B. Every item from A can be round-tripped through B, but not every B can be mapped to A. So
Injection is like a pair of function: `A => B, B => Option[A]`.

```scala
import com.twitter.bijection._

scala> Injection[Int, String](100)
res0: String = 100

scala> Injection.invert[Int, String](res0)
res1: Option[Int] = Some(100)
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
res2: Option[Long] = Some(123456789)
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

scala> long2String2Bytes2B64.invert(res5)
res1: Option[Long] = Some(243)
```

## Supported Bijections/Injections

Bijection implicitly supplies Bijections between:

* all numeric types <-> their boxed java counterparts
* containers/primitives <-> Json (Injections via bijection-json)
* thrift/protobuf <-> Array[Byte] (Injections via bijection-protobuf/bijection-thrift)
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

## Authors

* Oscar Boykin <http://twitter.com/posco>
* Marius Eriksen <http://twitter.com/marius>
* Sam Ritchie <http://twitter.com/sritchie>

## License

Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
