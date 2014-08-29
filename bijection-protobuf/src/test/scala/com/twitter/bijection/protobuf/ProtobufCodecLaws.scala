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

package com.twitter.bijection.protobuf

import com.twitter.bijection.{ BaseProperties, Bijection }
import com.twitter.bijection.protobuf.TestMessages.{ FatigueCount, Gender }
import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import org.scalacheck.Arbitrary
import org.scalatest._

class ProtobufCodecLaws extends PropSpec with PropertyChecks with MustMatchers with BaseProperties {
  def buildFatigueCount(tuple: (Long, Long, Int)) =
    FatigueCount.newBuilder()
      .setTargetId(tuple._1)
      .setSuggestedId(tuple._2)
      .setServeCount(tuple._3)
      .build

  implicit val fatigueCount: Arbitrary[FatigueCount] =
    arbitraryViaFn { input: (Long, Long, Int) => buildFatigueCount(input) }

  property("round trips protobuf -> Array[Byte]") {
    implicit val b = ProtobufCodec[FatigueCount]
    isLooseInjection[FatigueCount, Array[Byte]]
  }
}

class ProtobufEnumTest extends WordSpec with Matchers with BaseProperties {
  "ProtocolMessageEnum should roundtrip through ProtobufCodec" in {
    implicit val b = ProtobufEnumCodec[Gender]
    val male = Gender.valueOf(0)
    assert(male == rt(male))

    val female = Gender.valueOf(1)
    assert(female == rt(female))

    assert(b.invert(2).isFailure == true)
  }
}
