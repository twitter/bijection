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
import org.apache.thrift.TBase
import org.scalacheck.Properties
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

object ThriftCodecLaws extends Properties("ThriftCodecs") with BaseProperties {
  // Code generator for thrift instances.
  def roundTripsThrift(bijection: Bijection[TestThriftStructure, Array[Byte]]) =
    forAll { (i: Int, s: String) =>
      val thrift = new TestThriftStructure().setANumber(i).setAString(s)
      thrift == rt(thrift)(bijection)
    }

  property("round trips thrift -> Array[Byte] through binary") =
    roundTripsThrift(BinaryThriftCodec[TestThriftStructure])

  property("round trips thrift -> Array[Byte] through compact") =
    roundTripsThrift(CompactThriftCodec[TestThriftStructure])

  property("round trips thrift -> Array[Byte] through json") =
    roundTripsThrift(JsonThriftCodec[TestThriftStructure])
}
