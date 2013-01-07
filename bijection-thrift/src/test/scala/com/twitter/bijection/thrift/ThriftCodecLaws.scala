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

package com.twitter.bijection.thrift

import com.twitter.bijection.{ BaseProperties, Bijection }
import org.scalacheck.Properties
import org.scalacheck.Arbitrary
import org.specs._

object ThriftCodecLaws extends Properties("ThriftCodecs") with BaseProperties {
  def buildThrift(i: (Int, String)) =
    new TestThriftStructure().setANumber(i._1).setAString(i._2)

  implicit val testThrift = arbitraryViaFn { is: (Int,String) => buildThrift(is) }

  // Code generator for thrift instances.
  def roundTripsThrift(bijection: Bijection[TestThriftStructure, Array[Byte]]) = {
    implicit val b = bijection
    roundTrips[TestThriftStructure, Array[Byte]]()
  }

  property("round trips thrift -> Array[Byte] through binary") =
    roundTripsThrift(BinaryThriftCodec[TestThriftStructure])

  property("round trips thrift -> Array[Byte] through compact") =
    roundTripsThrift(CompactThriftCodec[TestThriftStructure])

  property("round trips thrift -> String through json") = {
    implicit val b = JsonThriftCodec[TestThriftStructure]
    roundTrips[TestThriftStructure, String]()
  }
}

class TEnumTest extends Specification with BaseProperties {
  "TEnum should roundtrip through TEnumCodec" in {
    implicit val b = TEnumCodec[Gender]
    val male = Gender.findByValue(0)
    male must_== rt(male)

    val female = Gender.findByValue(1)
    female must_== rt(female)
  }
}
