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

import org.scalacheck.Arbitrary

class CollectionLaws extends CheckProperties with BaseProperties {
  import com.twitter.bijection.StringArbs._

  implicit def vectorArb[A](implicit la: Arbitrary[List[A]]): Arbitrary[Vector[A]] =
    arbitraryViaFn[List[A], Vector[A]]((l: List[A]) => Vector(l: _*))

  implicit def seqArb[A](implicit la: Arbitrary[List[A]]): Arbitrary[Seq[A]] =
    arbitraryViaFn[List[A], Seq[A]]((l: List[A]) => Seq(l: _*))

  implicit def indexedSeqArb[A](implicit la: Arbitrary[List[A]]): Arbitrary[IndexedSeq[A]] =
    arbitraryViaFn[List[A], IndexedSeq[A]]((l: List[A]) => IndexedSeq(l: _*))

  implicit def traversableArb[A](implicit la: Arbitrary[List[A]]): Arbitrary[Traversable[A]] =
    arbitraryViaFn[List[A], Traversable[A]]((l: List[A]) => l.toTraversable)

  property("round trip List[Int] <=> Vector[String @@ Rep[Int]]") {
    type R = String @@ Rep[Int]
    type VR = Vector[R]
    //having this in scope guides the implicit resolution to success,
    //but only if we're using a type alias for the right side of isBijection.
    //that sounds like a scalac bug.
    //TODO: Should be minimized and reported
    implicit val i1: Bijection[Int, R] = Bijection.fromInjection(Injection.int2String)
    isBijection[List[Int], VR]
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

  property("Seq[Long] <=> IndexedSeq[Long]") {
    isBijection[Seq[Long], IndexedSeq[Long]]
  }

  property("Seq[Long] <=> List[Long]") {
    isBijection[Seq[Long], List[Long]]
  }

  property("IndexedSeq[Long] <=> IndexedSeq[(Int, Int)]") {
    isBijection[IndexedSeq[Long], IndexedSeq[(Int, Int)]]
  }

  property("List[Int] <=> IndexedSeq[String @@ Rep[Int]]") {
    isBijection[List[Int], IndexedSeq[String @@ Rep[Int]]]
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

  property("round trip Set[Int] -> Vector[String]") {
    isInjection[Set[Int], Vector[String]]
  }

  property("round trip List[Int] -> List[String]") {
    isInjection[List[Int], List[String]]
  }

  property("round trip Set[Int] -> Set[String]") {
    isInjection[Set[Int], Set[String]]
  }

}
