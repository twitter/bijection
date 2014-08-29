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

import com.twitter.bijection.{ BaseProperties, Bijection, Injection }
import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import org.scalacheck.Arbitrary
import org.scalatest._

class ThriftCodecLaws extends PropSpec with PropertyChecks with MustMatchers with BaseProperties {
  def buildThrift(i: (Int, String)) =
    new TestThriftStructure().setANumber(i._1).setAString(i._2)

  implicit val testThrift = arbitraryViaFn { is: (Int, String) => buildThrift(is) }

  // Code generator for thrift instances.
  def roundTripsThrift(implicit injection: Injection[TestThriftStructure, Array[Byte]]) = {
    isLooseInjection[TestThriftStructure, Array[Byte]]
  }

  property("round trips thrift -> Array[Byte] through binary") {
    roundTripsThrift(BinaryThriftCodec[TestThriftStructure])
  }

  property("round trips thrift -> Array[Byte] through compact") {
    roundTripsThrift(CompactThriftCodec[TestThriftStructure])
  }

  property("round trips thrift -> String through json") {
    implicit val b = JsonThriftCodec[TestThriftStructure]
    isLooseInjection[TestThriftStructure, String]
  }
}

class TEnumTest extends WordSpec with Matchers with BaseProperties {
  "TEnum should roundtrip through TEnumCodec" in {
    implicit val b = TEnumCodec[Gender]
    val male = Gender.findByValue(0)
    assert(male == rt(male))

    val female = Gender.findByValue(1)
    assert(female == rt(female))

    assert(b.invert(2).isFailure == true)
  }
}
