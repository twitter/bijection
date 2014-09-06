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

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import org.scalacheck.Gen._
import org.scalacheck.Arbitrary
import org.scalacheck.Prop._

import java.util.UUID
import java.net.URL

object StringArbs extends BaseProperties {
  import Rep._

  implicit val strByte = arbitraryViaBijection[Byte, String @@ Rep[Byte]]
  implicit val strShort = arbitraryViaBijection[Short, String @@ Rep[Short]]
  implicit val strInt = arbitraryViaBijection[Int, String @@ Rep[Int]]
  implicit val strLong = arbitraryViaBijection[Long, String @@ Rep[Long]]
  implicit val strFloat = arbitraryViaBijection[Float, String @@ Rep[Float]]
  implicit val strDouble = arbitraryViaBijection[Double, String @@ Rep[Double]]
}

class StringBijectionLaws extends PropSpec with PropertyChecks with MustMatchers
  with BaseProperties {
  import StringArbs._

  property("round trips string -> Array[String]") {
    isLooseInjection[String, Array[Byte]]
  }

  implicit val symbol = arbitraryViaFn { (s: String) => Symbol(s) }
  property("round trips string -> symbol") {
    isBijection[String, Symbol]
  }

  implicit val uuidArb = Arbitrary {
    for (
      l <- choose(-100L, 100L);
      u <- choose(-100L, 100L)
    ) yield (new UUID(l, u))
  }

  property("UUID -> String") {
    isInjection[UUID, String]
  }

  //property("UUID <-> String @@ Rep[UUID]") {
  // isBijection[UUID, String @@ Rep[UUID]]()
  // }

  def toUrl(s: String): Option[URL] =
    try { Some(new URL("http://" + s + ".com")) }
    catch { case _: Throwable => None }

  implicit val urlArb = Arbitrary {
    implicitly[Arbitrary[String]]
      .arbitrary
      .map { toUrl(_) }
      .filter { _.isDefined }
      .map { _.get }
  }

  // This is trivially a bijection if it injective
  property("URL -> String") {
    isInjection[URL, String]
  }

  property("rts through StringJoinBijection") {
    forAll { (sep: String, xs: List[String]) =>
      val sjBij = StringJoinBijection(sep)
      val iter = xs.toIterable
      whenever(!iter.exists(_.contains(sep))) {
        assert(iter == rt(iter)(sjBij))
      }
    }
  }

  //implicit val listOpt = StringJoinBijection.viaContainer[Int, List[Int]]()
  //property("viaCollection List[Int] -> Option[String]") =
  //roundTrips[List[Int], Option[String @@ Rep[List[Int]]]]()
  // implicit val listStr = StringJoinBijection.nonEmptyValues[Int, List[Int]]()
  // property("viaCollection List[Int] -> String") {
  // roundTrips[List[Int], String @@ Rep[List[Int]]]()
  // }

}
