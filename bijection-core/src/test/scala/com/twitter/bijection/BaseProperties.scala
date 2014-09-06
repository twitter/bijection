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
import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import org.scalacheck.Prop.forAll

import scala.math.Equiv
import scala.util.Success
import java.util.Arrays

trait BaseProperties {
  implicit def barrEq[T](implicit eqt: Equiv[T]): Equiv[Array[T]] = new Equiv[Array[T]] {
    def equiv(a1: Array[T], a2: Array[T]) =
      a1.zip(a2).forall { tup: (T, T) => eqt.equiv(tup._1, tup._2) }
  }

  def rt[A, B](a: A)(implicit bij: Bijection[A, B]): A = rtInjective[A, B](a)

  def rtInjective[A, B](a: A)(implicit bij: Bijection[A, B]): A = bij.invert(bij(a))

  /**
   * Checks that we can always invert all A
   * does not requires that all B that return Some[A] return to exact same B
   */
  def isLooseInjection[A, B](implicit arba: Arbitrary[A],
    inj: Injection[A, B], eqa: Equiv[A]) =
    forAll { (a: A) =>
      val b = inj(a)
      b != null &&
        {
          val bofa = inj.invert(b)
          bofa != null &&
            bofa.isSuccess &&
            eqa.equiv(bofa.get, a)
        }
    }

  def invertIsStrict[A, B](implicit arbb: Arbitrary[B], inj: Injection[A, B], eqb: Equiv[B]) =
    forAll { (b: B) =>
      inj.invert(b) match {
        case Success(aofb) =>
          assert(aofb != null, "aofb was null")
          eqb.equiv(b, inj(aofb))
        case _ => true // the failing case of None previously returned true?
      }
    }

  def isInjection[A, B](implicit a: Arbitrary[A],
    inj: Injection[A, B], barb: Arbitrary[B], eqa: Equiv[A], eqb: Equiv[B]) =
    isLooseInjection[A, B] && invertIsStrict[A, B]

  def isInjective[A, B](implicit a: Arbitrary[A], bij: ImplicitBijection[A, B], eqa: Equiv[A]) =
    forAll { (a: A) => eqa.equiv(a, rt(a)(bij.bijection)) }

  def invertIsInjection[A, B](implicit b: Arbitrary[B], bij: ImplicitBijection[A, B], eqb: Equiv[B]) =
    forAll { b: B => eqb.equiv(b, rtInjective(b)(bij.bijection.inverse)) }

  def isBijection[A, B](implicit arba: Arbitrary[A],
    arbb: Arbitrary[B], bij: ImplicitBijection[A, B], eqa: Equiv[A], eqb: Equiv[B]) = {
    implicit val inj = Injection.fromBijection(bij.bijection)
    isInjective[A, B] && invertIsInjection[A, B]
  }

  def arbitraryViaBijection[A, B](implicit bij: Bijection[A, B], arb: Arbitrary[A]): Arbitrary[B] =
    Arbitrary { arb.arbitrary.map { bij(_) } }
  def arbitraryViaFn[A, B](fn: A => B)(implicit arb: Arbitrary[A]): Arbitrary[B] =
    Arbitrary { arb.arbitrary.map { fn(_) } }
  def arbitraryViaInjection[A, B](implicit inj: Injection[A, B], arb: Arbitrary[A]): Arbitrary[B] =
    Arbitrary { arb.arbitrary.map { inj(_) } }
}
