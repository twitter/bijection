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

package com.twitter.bijection.guava

import com.google.common.base.Optional
import com.google.common.base.{ Function => GFn, Predicate, Supplier }
import com.twitter.bijection.{ @@, BaseProperties, Bijection, Rep, Conversion }
import com.twitter.bijection.Rep._

import org.scalacheck.Arbitrary
import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import org.scalacheck.Prop.forAll

import java.lang.{ Long => JLong }

import Bijection.connect
import Conversion.asMethod

class GuavaBijectionLaws extends PropSpec with PropertyChecks with MustMatchers with BaseProperties {
  import GuavaBijections._

  implicit def arbOptional[T: Arbitrary] =
    arbitraryViaFn[T, Optional[T]] { Optional.of(_) }

  property("round trips Option[Int] -> Optional[Int]") {
    isBijection[Option[Int], Optional[Int]]
  }

  property("round trips Option[Long] -> Optional[Long]") {
    isBijection[Option[Long], Optional[Long]]
  }

  def roundTripsFn[A, B](fn: A => B)(implicit arb: Arbitrary[A], bij: Bijection[A => B, GFn[A, B]], eqb: Equiv[B]) = {
    val rtFn = bij(fn)
    forAll { a: A => assert(eqb.equiv(fn(a), rtFn.apply(a))) }
  }

  property("round trips Int => Long -> GuavaFn[Int, Long]") {
    roundTripsFn[Int, Long] { x => (x * x).toLong }
  }

  property("round trips () => Long -> Supplier[JLong]") {
    forAll { l: Long =>
      val fn = { () => l }
      assert(fn() == fn.as[Supplier[JLong]].get.as[Long])
    }
  }

  property("round trips Long => Boolean -> Predicate[JLong]") {
    forAll { l: Long =>
      val isEven = { l: Long => l % 2 == 0 }
      assert(isEven(l) == isEven.as[Predicate[JLong]].apply(l.as[JLong]))
    }
  }
}
