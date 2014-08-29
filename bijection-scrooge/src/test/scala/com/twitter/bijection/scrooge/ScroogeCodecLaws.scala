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

import com.twitter.bijection.{ BaseProperties, Bijection, Injection }

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import org.scalacheck.Arbitrary

class ScroogeCodecLaws extends PropSpec with PropertyChecks with MustMatchers with BaseProperties {
  def buildScrooge(i: (Int, String)) =
    TestStruct(i._1, Some(i._2))

  implicit val testScrooge = arbitraryViaFn { is: (Int, String) => buildScrooge(is) }

  // Code generator for thrift instances.
  def roundTripsScrooge(bijection: Injection[TestStruct, Array[Byte]]) = {
    implicit val b = bijection
    isLooseInjection[TestStruct, Array[Byte]]
  }

  property("round trips thrift -> Array[Byte] through binary") {
    roundTripsScrooge(BinaryScalaCodec(TestStruct))
  }

  property("round trips thrift -> Array[Byte] through compact") {
    roundTripsScrooge(CompactScalaCodec(TestStruct))
  }
}
