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

package com.twitter.bijection.json

import com.twitter.bijection.{ BaseProperties, Bijection }
import org.scalacheck.Properties
import org.scalacheck.Arbitrary

object JsonBijectionLaws extends Properties("JsonBijection") with BaseProperties {
  def roundTripJson[T: JsonObject : Arbitrary] = {
    implicit val bij: Bijection[T,String] = new JsonBijection[T]
    roundTrips[T,String]()
  }

  property("Short") = roundTripJson[Short]
  property("Int") = roundTripJson[Int]
  property("Long") = roundTripJson[Long]
  property("Double") = roundTripJson[Double]
  property("String") = roundTripJson[String]
  // Collections
  property("Map[String,Int]") = roundTripJson[Map[String,Int]]
  property("Map[String,Long]") = roundTripJson[Map[String,Long]]
  property("Map[String,String]") = roundTripJson[Map[String,String]]
  property("Map[String,Map[String,Int]]") = roundTripJson[Map[String,Map[String,Int]]]
  property("List[String]") = roundTripJson[List[String]]
  property("List[Int]") = roundTripJson[List[Int]]
  property("List[List[String]]") = roundTripJson[List[List[String]]]

}
