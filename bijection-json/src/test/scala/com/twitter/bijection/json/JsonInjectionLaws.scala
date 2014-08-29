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

import com.twitter.bijection.Conversion.asMethod

import com.twitter.bijection.{ BaseProperties, Bijection, Injection }
import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import org.scalacheck.Prop.forAll
import org.scalacheck.Arbitrary

import org.codehaus.jackson.JsonNode
import com.twitter.bijection.json.JsonNodeInjection.{ fromJsonNode, toJsonNode }
import scala.util.Try

class JsonInjectionLaws extends PropSpec with PropertyChecks with MustMatchers with BaseProperties {
  // Needed from some recursive injections (like tuples)
  import JsonNodeInjection._

  def roundTripJson[T: JsonNodeInjection: Arbitrary: Equiv] = {
    implicit val bij: Injection[T, String] = JsonInjection.toString[T]
    isLooseInjection[T, String]
  }

  property("Short") {
    roundTripJson[Short]
  }

  property("Int") {
    roundTripJson[Int]
  }

  property("Long") {
    roundTripJson[Long]
  }

  property("Float") {
    roundTripJson[Float]
  }

  property("Double") {
    roundTripJson[Double]
  }

  property("String") {
    roundTripJson[String]
  }

  property("Array[Byte]") {
    roundTripJson[Array[Byte]]
  }

  // Collections
  property("(String,Int)") {
    roundTripJson[(String, Int)]
  }

  property("(String,Int,Long)") {
    roundTripJson[(String, Int, Long)]
  }

  property("(String,(String,Int))") {
    roundTripJson[(String, (String, Int))]
  }

  property("Either[String,Int]") {
    roundTripJson[Either[String, Int]]
  }

  property("Map[String, Either[String,Int]]") {
    roundTripJson[Map[String, Either[String, Int]]]
  }

  property("Map[String,Int]") {
    roundTripJson[Map[String, Int]]
  }

  property("Map[String,Long]") {
    roundTripJson[Map[String, Long]]
  }

  property("Map[String,String]") {
    roundTripJson[Map[String, String]]
  }

  property("Map[String,Map[String,Int]]") {
    roundTripJson[Map[String, Map[String, Int]]]
  }

  property("List[String]") {
    roundTripJson[List[String]]
  }

  property("List[Int]") {
    roundTripJson[List[Int]]
  }

  property("List[List[String]]") {
    roundTripJson[List[List[String]]]
  }

  property("Set[Int]") {
    roundTripJson[Set[Int]]
  }

  implicit val jsonOpt = JsonNodeInjection.viaInjection[Option[Int], List[Int]]
  property("Option[Int]") {
    roundTripJson[Option[Int]]
  }

  implicit val arbSeq: Arbitrary[Seq[Int]] = arbitraryViaFn { s: List[Int] => s.toSeq }
  implicit val arbVec: Arbitrary[Vector[Int]] = arbitraryViaFn { s: List[Int] => Vector(s: _*) }
  implicit val arbIndexed: Arbitrary[IndexedSeq[Int]] = arbitraryViaFn { s: List[Int] => s.toIndexedSeq }

  property("Seq[Int]") {
    roundTripJson[Seq[Int]]
  }

  property("Vector[Int]") {
    roundTripJson[Vector[Int]]
  }

  property("IndexedSeq[Int]") {
    roundTripJson[IndexedSeq[Int]]
  }

  // Handle Mixed values:
  property("Mixed values") {
    forAll { (kv: List[(String, Int, List[String])]) =>

      val mixedMap = kv.map {
        case (key, intv, lv) =>
          if (scala.math.random < 0.5) { (key + "i", toJsonNode(intv)) }
          else { (key + "l", toJsonNode(lv)) }
      }.toMap

      val jsonMixed = mixedMap.as[UnparsedJson]

      jsonMixed.as[Try[Map[String, JsonNode]]].get.foreach { kup: (String, JsonNode) =>
        val (k, up) = kup
        if (k.endsWith("i")) {
          assert(fromJsonNode[Int](up).get == fromJsonNode[Int](mixedMap(k)).get)
        } else {
          assert(fromJsonNode[List[String]](up).get == fromJsonNode[List[String]](mixedMap(k)).get)
        }
      }
    }
  }

}
