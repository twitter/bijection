/*
 * Copyright 2010 Twitter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.bijection.scrooge

import com.twitter.bijection.{ CheckProperties, BaseProperties, Bijection, Injection }

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import org.scalacheck.{ Gen, Arbitrary }

class ScroogeCodecLaws extends CheckProperties with BaseProperties {
  implicit val testScrooge: Arbitrary[TestStruct] = Arbitrary {
    for {
      l <- Gen.oneOf('a' to 'z')
      u <- Gen.oneOf('A' to 'Z')
      s <- Gen.listOf(Gen.oneOf(l, u, Gen.numChar)).map(_.mkString)
      i <- Arbitrary.arbitrary[Int]
    } yield TestStruct(i, Some(s))
  }

  // Code generator for thrift instances.
  def roundTripsScrooge[B](bijection: Injection[TestStruct, B]) = {
    implicit val b = bijection
    isLooseInjection[TestStruct, B]
  }

  property("round trips thrift -> Array[Byte] through binary") {
    roundTripsScrooge(BinaryScalaCodec(TestStruct))
  }

  property("round trips thrift -> Array[Byte] through compact") {
    roundTripsScrooge(CompactScalaCodec(TestStruct))
  }
  property("round trips thrift -> Json through json codec") {
    roundTripsScrooge[String](JsonScalaCodec(TestStruct))
  }
}

