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

class CollectionLaws extends PropSpec with PropertyChecks with MustMatchers
  with BaseProperties {
  import StringArbs._

  implicit def vectorArb[A](implicit la: Arbitrary[List[A]]) =
    arbitraryViaFn { (l: List[A]) => Vector(l: _*) }
  implicit def seqArb[A](implicit la: Arbitrary[List[A]]) =
    arbitraryViaFn { (l: List[A]) => Seq(l: _*) }
  implicit def indexedSeqArb[A](implicit la: Arbitrary[List[A]]) =
    arbitraryViaFn { (l: List[A]) => IndexedSeq(l: _*) }
  implicit def traversableArb[A](implicit la: Arbitrary[List[A]]) =
    arbitraryViaFn { (l: List[A]) => l.toTraversable }

  property("round trip List[Int] <=> Vector[String @@ Rep[Int]]") {
    isBijection[List[Int], Vector[String @@ Rep[Int]]]
  }

  property("round trip List[long] <=> List[String @@ Rep[Int]]") {
    isBijection[List[Long], List[String @@ Rep[Long]]]
  }

  property("round trip Vector[Double] <=> Vector[String @@ Rep[Double]]") {
    isBijection[Vector[Double], Vector[String @@ Rep[Double]]]
  }

  property("round trip Set[Double] <=> Set[String @@ Rep[Double]]") {
    isBijection[Set[Double], Set[String @@ Rep[Double]]]
  }

  property("round trip Seq[Double] <=> Seq[String @@ Rep[Double]]") {
    isBijection[Seq[Double], Seq[String @@ Rep[Double]]]
  }

  property("round trip Map[Long,Double] <=> Map[String @@ Rep[Long], String @@ Rep[Double]]") {
    isBijection[Map[Long, Double], Map[String @@ Rep[Long], String @@ Rep[Double]]]
  }

  property("Option[Long] <=> Option[String @@ Rep[Long]]") {
    isBijection[Option[Long], Option[String @@ Rep[Long]]]
  }

  property("Array[Int] <=> Seq[Int]") {
    isBijection[Array[Int], Seq[Int]]
  }

  property("Array[Int] <=> Traversable[Int]") {
    isBijection[Array[Int], Traversable[Int]]
  }

  property("Vector[Int] <=> Seq[Int]") {
    isBijection[Vector[Int], Seq[Int]]
  }

  property("Vector[Int] <=> IndexedSeq[Int]") {
    isBijection[Vector[Int], IndexedSeq[Int]]
  }

  property("List[Int] <=> IndexedSeq[String @@ Rep[Int]]") {
    isBijection[List[Int], IndexedSeq[String @@ Rep[Int]]]
  }

  property("List[Int] <=> Vector[String @@ Rep[Int]]") {
    isBijection[List[Int], Vector[String @@ Rep[Int]]]
  }

  property("Option[Int] <=> Option[Long]") {
    isInjection[Option[Int], Option[Long]]
  }

  property("Map[Int, Short] -> Set[(Int, Short)]") {
    isInjection[Map[Int, Short], Set[(Int, Short)]]
  }

  property("round trip Set[Int] -> List[String]") {
    isInjection[Set[Int], List[String]]
  }
}
