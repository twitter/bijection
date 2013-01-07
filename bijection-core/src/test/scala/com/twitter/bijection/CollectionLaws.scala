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

object CollectionLaws extends Properties("Collections")
with BaseProperties {
  implicit val listToVector = Bijection.toContainer[Int, String, List[Int], Vector[String]]
  property("round trip List[Int] -> Vector[String]") = roundTrips[List[Int], Vector[String]]()

  implicit val setToIter = Bijection.toContainer[Int, String, Set[Int], Iterable[String]]
  property("round trip Set[Int] -> Iterable[String]") = roundTrips[Set[Int], Iterable[String]]()
}
