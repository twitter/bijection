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

object StringArbs extends BaseProperties {
  import Rep._

  implicit val strByte = arbitraryViaFn { (b: Byte) => b.toString.toRep[Byte].get }
  implicit val strShort = arbitraryViaFn { (b: Short) => b.toString.toRep[Short].get }
  implicit val strInt: Arbitrary[String @@ Rep[Int]] =
    arbitraryViaFn { (b: Int) => b.toString.toRep[Int].get }
  implicit val strLong = arbitraryViaFn { (b: Long) => b.toString.toRep[Long].get }
  implicit val strFloat = arbitraryViaFn { (b: Float) => b.toString.toRep[Float].get }
  implicit val strDouble = arbitraryViaFn { (b: Double) => b.toString.toRep[Double].get }
}

object StringBijectionLaws extends Properties("StringBijections")
with BaseProperties {
  import StringArbs._

  implicit val bij: Bijection[String, Array[Byte]] = StringCodec.utf8
  // TODO: add Array[Byte] @@ Rep[Utf8] and make it a bijection
  property("round trips string -> Array[String]") = isInjection[String, Array[Byte]]()
  implicit val symbol = arbitraryViaFn { (s: String) => Symbol(s) }
  property("round trips string -> symbol") = isBijection[String, Symbol]()

  implicit val uuidArb = Arbitrary {
    for( l <- choose(-100L, 100L);
         u <- choose(-100L, 100L)) yield (new UUID(l,u))
  }
  property("round trip UUID -> String") = roundTrips[UUID, String @@ Rep[UUID]]()
  def toUrl(s: String): Option[URL] =
    try { Some(new URL("http://" + s + ".com")) }
    catch { case _ => None }

  implicit val urlArb = Arbitrary { implicitly[Arbitrary[String]]
    .arbitrary
    .map { toUrl(_) }
    .filter { _.isDefined }
    .map { _.get }
  }
  // This is trivially a bijection if it injective
  property("round trip URL -> String") = isInjection[URL, String @@ Rep[URL]]()

  property("rts through StringJoinBijection") =
    forAll { (sep: String, xs: List[String]) =>
      val sjBij = StringJoinBijection(sep)
      val iter = xs.toIterable
      (!iter.exists(_.contains(sep))) ==> (iter == rt(iter)(sjBij))
    }

  //implicit val listOpt = StringJoinBijection.viaContainer[Int, List[Int]]()
  //property("viaCollection List[Int] -> Option[String]") =
  //roundTrips[List[Int], Option[String @@ Rep[List[Int]]]]()
  // implicit val listStr = StringJoinBijection.nonEmptyValues[Int, List[Int]]()
  // property("viaCollection List[Int] -> String") = roundTrips[List[Int], String @@ Rep[List[Int]]]()

}
