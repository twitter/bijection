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

package com.twitter.bijection

import org.scalacheck.Properties
import org.scalacheck.Gen._
import org.scalacheck.Arbitrary
import org.scalacheck.Prop._

import java.util.UUID
import java.net.URL

object StringBijectionLaws extends Properties("StringBijections")
with BaseProperties {
  implicit val bij: Bijection[String, Array[Byte]] = StringCodec.utf8
  property("round trips string -> Array[String]") = roundTrips[String, Array[Byte]]()
  property("round trips string -> symbol") = roundTrips[String, Symbol]()

  implicit val uuidArb = Arbitrary {
    for( l <- choose(-100L, 100L);
         u <- choose(-100L, 100L)) yield (new UUID(l,u))
  }
  property("round trip UUID -> String") = roundTrips[UUID, String]()
  def toUrl(s: String): Option[URL] =
    try { Some(new URL("http://" + s + ".com")) }
    catch { case _ => None }

  implicit val urlArb = Arbitrary { implicitly[Arbitrary[String]]
    .arbitrary
    .map { toUrl(_) }
    .filter { _.isDefined }
    .map { _.get }
  }
  property("round trip URL -> String") = roundTrips[URL, String]()

  property("rts through StringJoinBijection") =
    forAll { (sep: String, xs: List[String]) =>
      val sjBij = StringJoinBijection(sep)
      val iter = xs.toIterable
      (!iter.exists(_.contains(sep))) ==> (iter == rt(iter)(sjBij))
    }

  // implicit val listOpt = StringJoinBijection.viaContainer[Int, List[Int]]()
  // property("viaCollection List[Int] -> Option[String]") = roundTrips[List[Int], Option[String @@ Rep[List[Int]]]]()
  // implicit val listStr = StringJoinBijection.nonEmptyValues[Int, List[Int]]()
  // property("viaCollection List[Int] -> String") = roundTrips[List[Int], String @@ Rep[List[Int]]]()

}
