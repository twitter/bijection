/*
Copyright 2015 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.twitter.bijection.finagle_mysql

import com.twitter.bijection.{CheckProperties, BaseProperties}
import org.scalacheck.Arbitrary
import com.twitter.finagle.mysql._
import java.sql.Timestamp

import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

class MySqlConversionLaws extends CheckProperties with BaseProperties {
  import MySqlConversions._

  implicit val byteValueArb: Arbitrary[ByteValue] =
    Arbitrary(arbitrary[Byte].map(ByteValue.apply))
  implicit val shortValueArb: Arbitrary[ShortValue] =
    Arbitrary(arbitrary[Short].map(ShortValue.apply))
  implicit val intValueArb: Arbitrary[IntValue] =
    Arbitrary(arbitrary[Int].map(IntValue.apply))
  implicit val longValueArb: Arbitrary[LongValue] =
    Arbitrary(arbitrary[Long].map(LongValue.apply))
  implicit val floatValueArb: Arbitrary[FloatValue] =
    Arbitrary(arbitrary[Float].map(FloatValue.apply))
  implicit val doubleValueArb: Arbitrary[DoubleValue] =
    Arbitrary(arbitrary[Double].map(DoubleValue.apply))
  implicit val stringValueArb: Arbitrary[StringValue] =
    Arbitrary(arbitrary[String].map(StringValue.apply))
  implicit val nullValueArb: Arbitrary[NullValue.type] =
    Arbitrary(Gen.const(NullValue))
  implicit val emptyValueArb: Arbitrary[EmptyValue.type] =
    Arbitrary(Gen.const(EmptyValue))

  val timeGenerator: Gen[Long] = Gen.choose(1L, 253375661380264L) // until year 9999

  implicit val timestampValueArb: Arbitrary[Value] = Arbitrary {
    val UTC = java.util.TimeZone.getTimeZone("UTC")
    val timestampValue = new TimestampValue(UTC, UTC)
    for {
      x <- timeGenerator
    } yield timestampValue(new Timestamp(x))
  }

  implicit val timestampArb: Arbitrary[Timestamp] = Arbitrary {
    for {
      x <- timeGenerator
    } yield new Timestamp(x)
  }

  property("Byte") {
    isBijection[ByteValue, Byte]
  }
  property("Short") {
    isBijection[ShortValue, Short]
  }
  property("Int") {
    isBijection[IntValue, Int]
  }
  property("Long") {
    isBijection[LongValue, Long]
  }
  property("Float") {
    isBijection[FloatValue, Float]
  }
  property("Double") {
    isBijection[DoubleValue, Double]
  }
  property("String") {
    isBijection[StringValue, String]
  }

  property("Boolean") {
    isInjection[Boolean, ByteValue]
  }
  property("Empty") {
    isInjection[EmptyValue.type, Option[Int]]
  }
  property("Null") {
    isInjection[NullValue.type, Option[String]]
  }
  property("Timestamp") {

    /**
      * Custom equivalence typeclass for RawValue
      * This is here to compare two RawValues generated from Timestamps.
      * Because they contain byte arrays, just =='ing them does not work as expected.
      */
    implicit val valueEquiv = new scala.math.Equiv[Value] {
      override def equiv(a: Value, b: Value) = (a, b) match {
        case (
            RawValue(Type.Timestamp, MysqlCharset.Binary, true, bytes1),
            RawValue(Type.Timestamp, MysqlCharset.Binary, true, bytes2)
            ) =>
          bytes1.toList == bytes2.toList
        case _ => false
      }
    }

    isInjection[Timestamp, Value]
  }
}
