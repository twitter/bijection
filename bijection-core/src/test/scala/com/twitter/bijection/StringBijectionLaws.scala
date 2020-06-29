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

import java.net.URL
import java.util.UUID

import org.scalacheck.Arbitrary
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

object StringArbs {
  import BaseProperties._

  implicit val strByte = arbitraryViaBijection[Byte, String @@ Rep[Byte]]
  implicit val strShort = arbitraryViaBijection[Short, String @@ Rep[Short]]
  implicit val strInt = arbitraryViaBijection[Int, String @@ Rep[Int]]
  implicit val strLong = arbitraryViaBijection[Long, String @@ Rep[Long]]
  implicit val strFloat = arbitraryViaBijection[Float, String @@ Rep[Float]]
  implicit val strDouble = arbitraryViaBijection[Double, String @@ Rep[Double]]
}

/**
  * We had an issue with giant strings. Make sure they work
  */
class StringRegressions extends AnyFunSuite {
  test("Strings larger that 2^24, the largest integer range floats can store work") {
    val bigString = Array.fill(70824427)(42.toByte)
    assert(Injection.utf8.invert(bigString).isSuccess)
  }
}

class StringBijectionLaws extends CheckProperties with BaseProperties {
  property("round trips string -> Array[String]") {
    isSerializableInjection[String, Array[Byte]]
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
    isSerializableInjection[UUID, String]
  }

  //property("UUID <-> String @@ Rep[UUID]") {
  // isBijection[UUID, String @@ Rep[UUID]]()
  // }

  def toUrl(s: String): Try[URL] = Try(new URL("http://" + s + ".com"))

  // Gen's identifier will produce string starting with a lower case alpha
  // followed by an alpha numeric sequence of characters
  implicit val urlArb: Arbitrary[URL] =
    Arbitrary { identifier map (toUrl(_)) suchThat (_.isSuccess) map (_.get) }

  // This is trivially a bijection if it injective
  property("URL -> String") {
    isSerializableInjection[URL, String]
  }

  property("rts through StringJoinBijection") {
    forAll { (sep: String, xs: List[String]) =>
      val sjBij = StringJoinBijection(sep)
      val iter: Iterable[String] = xs
      (!iter.exists(_.contains(sep))) ==> (iter == rt(iter)(sjBij))
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
