# Bijection #

### 0.1.1

* Added `bijection-json`

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
