# Bijection #

### 0.9.1
* Repro ser/de failures and add tests: https://github.com/twitter/bijection/pull/241
* Fix flaky URL test: https://github.com/twitter/bijection/pull/240

### 0.9.0
* Reduce the exposed type granularity down, maintains binary compat wit…: https://github.com/twitter/bijection/pull/235
* avro binary with schema: https://github.com/twitter/bijection/pull/238
* Make string Injections safer. Fix #199: https://github.com/twitter/bijection/pull/237
* Mostly maintenance commit: https://github.com/twitter/bijection/pull/234
* set the max file size to deal with #184: https://github.com/twitter/bijection/pull/232
* Getting rid of a circular dependency between bijection-macros and chill: https://github.com/twitter/bijection/pull/228
* Noticed this appearing in downstream bundles, should be for test only: https://github.com/twitter/bijection/pull/226
* Upgrade sbt launcher script (sbt-extras): https://github.com/twitter/bijection/pull/225
* En-threadsafe json injections for collections: https://github.com/twitter/bijection/pull/224

### 0.8.1
* Simplifies hbase injections with fastAttempt macro: https://github.com/twitter/bijection/pull/220
* Specialize the TBinaryProtocol read path up to ~2x speedups: https://github.com/twitter/bijection/pull/221
* Makes some of the hbase injection tests stricter: https://github.com/twitter/bijection/pull/219
* Migrates hbase bijections to injections because they weren't actually bijections: https://github.com/twitter/bijection/pull/217
* Removes unnecessary HBase injections: https://github.com/twitter/bijection/pull/215
* Fixes flaky test: https://github.com/twitter/bijection/pull/214
* Fixup unidoc: https://github.com/twitter/bijection/pull/213
* Update travis & sbt scala versions to 2.10.5 and 2.11.5: https://github.com/twitter/bijection/pull/212

### 0.8.0
* add twitter util Buf <-> Array[Byte] bijection https://github.com/twitter/bijection/pull/208
* upgrade testing libraries https://github.com/twitter/bijection/pull/207
* Add implicit conversion for GZippedBase64String to String https://github.com/twitter/bijection/pull/203
* Make property tests work again https://github.com/twitter/bijection/pull/198
* Add a module for finagle-mysql bijections. https://github.com/twitter/bijection/pull/197
* Build bijection-scrooge for scala 2.11 https://github.com/twitter/bijection/pull/196

### 0.7.2
* FIX: gzip Bijection resource leak. https://github.com/twitter/bijection/pull/193
* Use new Travis CI infrastructure https://github.com/twitter/bijection/pull/191

### 0.7.1
* Remove some package privacy so these things can be used in scalding and ...: https://github.com/twitter/bijection/pull/190
* Add macros to create Trys: https://github.com/twitter/bijection/pull/187
* Refactor macros: https://github.com/twitter/bijection/pull/186
* Add TypeclassBijection: https://github.com/twitter/bijection/pull/183
* Generate some useful case class conversions with macros: https://github.com/twitter/bijection/pull/179

### 0.7.0
* Added thrift json injections: https://github.com/twitter/bijection/pull/172
* Make almost all the case classes extend AnyVal: https://github.com/twitter/bijection/pull/178
* Fix ImplicitBijection issue: https://github.com/twitter/bijection/pull/177
* Update jackson-mapper-asl to version 1.9.2: https://github.com/twitter/bijection/pull/155
* Moves to 2.10.x as the default and to scalatest: https://github.com/twitter/bijection/pull/176
* Fix some bugs in ModDivInjections: https://github.com/twitter/bijection/pull/175
* Add compression support to bijection-avro: https://github.com/twitter/bijection/pull/174
* Injections from jodatime LocalDate / LocalTime / YearMonth / MonthDay to String: https://github.com/twitter/bijection/pull/171

### 0.6.3
* Sbt => 0.13, use scalariform: https://github.com/twitter/bijection/pull/170
* Backtick Array[Byte] in README: https://github.com/twitter/bijection/pull/168
* Update README to include bijection-avro: https://github.com/twitter/bijection/pull/167
* Update build publishTo to be consistent with scalding: https://github.com/twitter/bijection/pull/165
* Pin bijection-guava to jsr305 1.3.9.: https://github.com/twitter/bijection/pull/164

### 0.6.2
* Added json4s to project aggregate: https://github.com/twitter/bijection/pull/161

### 0.6.1
* Add the sbt version helper script: https://github.com/twitter/bijection/pull/160
* Added Json4s Injections: https://github.com/twitter/bijection/pull/157
* Added url encoded String Inection: https://github.com/twitter/bijection/pull/156
* Added bytes2bytesWritable bijection: https://github.com/twitter/bijection/pull/154
* Update README.md: https://github.com/twitter/bijection/pull/153

### 0.6.0
* Update Avro: https://github.com/twitter/bijection/pull/147/files
* Fix a thread-safety bug with collection Bufferables: https://github.com/twitter/bijection/pull/150
* Add Boolean support for Bufferable: https://github.com/twitter/bijection/pull/151

### 0.5.4

* Fix Scrooge Import for Scala 2.10: https://github.com/twitter/bijection/pull/145
* Add Community Section to README: https://github.com/twitter/bijection/pull/143
* Added Hbase Injections: https://github.com/twitter/bijection/pull/144

### 0.5.3

* SBT CodeGen: https://github.com/twitter/bijection/pull/128
* Remove redundant Attempt (in favor of scala.util.Try) https://github.com/twitter/bijection/pull/133
* Adds bijection-avro: https://github.com/twitter/bijection/pull/129
* Adds bijection-hbase: https://github.com/twitter/bijection/pull/135
* Injection from Bijection with Rep tag: https://github.com/twitter/bijection/pull/138
* Move bijection-algebird to algebird: https://github.com/twitter/bijection/pull/139
* Adds bijection-jodatime: https://github.com/twitter/bijection/pull/136
* Replace scrooge-runtime with scrooge-serializer: https://github.com/twitter/bijection/pull/141

### 0.5.2

* Remove withSources

### 0.5.1

* Lets bijection-util be distributed
* Correct README

### 0.5.0

* Make Bijection/Injection not extend Function
* Add support for scala Futures & Try
* Either injections
* Add java Base64 from Apache commons

### 0.4.1

* Added `Codec[T]` alias for serialization injections.

### 0.4.0

* `bijection-netty` for async functionality. These help with Finagle stores.
* JavaSerializationInjection
* ModDivInjection
* fix CastInjection
* JsonInjection

### 0.3.0

* Added `Injection` typeclass
* Autogenerated Tuple* -> List injections
* Removed all unsafe Bijections

### 0.2.1

* `bijection-guava` for Guava interop.
* `bijection-algebird` for bijections on classes in twitter's [Algebird](https://github.com/twitter/algebird).
* `bijection-guava` for Guava interop, with
  * Function1 <-> Guava Function
  * () => T <-> Supplier[T]
  * Function[T, Boolean] <-> Predicate[T]
  * Optional[T] <-> Option[T]
* `bijection-util` with bijections on [twitter-util](https://github.com/twitter/util)'s "Try" and "Future".

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
