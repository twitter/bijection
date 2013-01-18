# Bijection #

### 0.2.1

### 0.2.0

* `bijection-scrooge` for scrooge generated scala thrift code
* Add tagged types, resolved Bijection debate
* Implicit bijection between `List[T]`, `List[U]`
* Implicit bijection between `Vector[T]`, `Vector[U]`
* Implicit bijection between `Set[T]`, `Set[U]`
* Implicit bijection between `Map[K1, V1]`, `Map[K2, V2]`

### 0.1.3

* remove `Bijection.build` uses, as these screw up serialization via Kryo.

### 0.1.2

* Removes simple-json
* Adds more sophisticated JSON codecs
* Removes `withSources()` from dependencies

### 0.1.1

* `bijection-json`
* `bijection-protobuf`
* `TEnumCodec` for conversion of `TEnum` -> `Int`
* Change `biject` method in `Bijection` object to `asMethod` for clarity
* Adds default `unwrap` bijections to value case classes
* Adds `Bijection.getOrElse` for conversions between `Option[A]` and `A`
* Fix bug in `toContainer`, add tests
* `Bufferable`
* `StringBijection.viaContainer`

### 0.1.0

* Adds Bijection trait with implicits between:
  * all numeric types <-> their boxed java counterparts
  * all numeric types <-> big-endian `Array[Byte]` encodings
  * all numeric types <-> String
  * Bijections for all `asScala`, `asJava` pairs provided by    [scala.collection.JavaConverters](http://www.scala-lang.org/api/current/scala/collection/JavaConverters$.html)
  * String <-> utf8 encoded bytes
  * `Array[Byte]` <-> `GZippedBytes`
  * `Array[Byte]` <-> `Base64String`
  * `Array[Byte]` <-> `GZippedBase64String`
  * `Array[Byte]` <-> `java.nio.ByteBuffer`
  * `Class[T]` <-> String
  * `A => B` <-> `C => D` (function conversion)
  * Bijection builders for all tuples. (`(String,Int)` <-> `(Array[Byte], java.lang.Integer)` is built automatically, for example.)
* Value classes for `Base64String`, `GZippedBase64String`, and `GZippedBytes`
* `as` casting conversion.
* `Pivot` trait for packing schemes
* `BijectionImpl` for easy java implementation
* Adds ThriftCodecs and `bijection-thrift`
