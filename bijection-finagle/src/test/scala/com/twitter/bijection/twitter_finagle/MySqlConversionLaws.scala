/*
Copyright 2012 Twitter, Inc.

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

package com.twitter.bijection.twitter_finagle

import com.twitter.bijection.{ BaseProperties, Bijection }
import org.scalacheck.Arbitrary
import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks
import com.twitter.finagle.exp.mysql._
import java.sql.Timestamp
import java.util.Date

import org.scalacheck.Prop.forAll
import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

class MySqlConversionLaws extends PropSpec with PropertyChecks with MustMatchers with BaseProperties {
  import MySqlConversions._

  implicit val byteValueArb = Arbitrary(arbitrary[Byte].map(ByteValue.apply))
  implicit val shortValueArb = Arbitrary(arbitrary[Short].map(ShortValue.apply))
  implicit val intValueArb = Arbitrary(arbitrary[Int].map(IntValue.apply))
  implicit val longValueArb = Arbitrary(arbitrary[Long].map(LongValue.apply))
  implicit val floatValueArb = Arbitrary(arbitrary[Float].map(FloatValue.apply))
  implicit val doubleValueArb = Arbitrary(arbitrary[Double].map(DoubleValue.apply))
  implicit val stringValueArb = Arbitrary(arbitrary[String].map(StringValue.apply))
  implicit val timestampValueArb = Arbitrary(arbitrary[Date].map(d => new Timestamp(d.getTime)))
  implicit val nullValueArb = Arbitrary(Gen.const(NullValue))
  implicit val emptyValueArb = Arbitrary(Gen.const(EmptyValue))
  implicit val valueArb: Arbitrary[Value] = Arbitrary(Gen.oneOf(arbitrary[ByteValue],
    arbitrary[NullValue.type],
    arbitrary[EmptyValue.type],
    arbitrary[ShortValue],
    arbitrary[IntValue],
    arbitrary[LongValue],
    arbitrary[FloatValue],
    arbitrary[DoubleValue],
    arbitrary[StringValue]))

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
    isBijection[ByteValue, Boolean]
  }

  property("Timestamp") {
    isInjection[Timestamp, Value]
  }
  property("Empty") {
    isInjection[EmptyValue.type, Option[Int]]
  }
  property("Null") {
    isInjection[NullValue.type, Option[String]]
  }
}
