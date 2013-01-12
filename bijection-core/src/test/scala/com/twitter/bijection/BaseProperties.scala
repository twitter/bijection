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

import org.scalacheck.{ Arbitrary, Properties }
import org.scalacheck.Prop.forAll

trait BaseProperties {

  def rt[A, B](a: A)(implicit bij: Bijection[A, B]): A = rtInjective[A,B](a)

  def rtInjective[A, B](a: A)(implicit bij: Bijection[A, B]): A = bij.invert(bij(a))

  def defaultEq[A](a1: A, a2: A) = a1 == a2
  def roundTrips[A, B](eqFn: (A, A) => Boolean = defaultEq _)
  (implicit a: Arbitrary[A], bij: Bijection[A, B]) =
    forAll { a: A => eqFn(a, rt(a)) }

  def isInjection[A,B](eqFn: (A,A) => Boolean = defaultEq _)
    (implicit a: Arbitrary[A], bij: Bijection[A, B]) =
      forAll { a: A => eqFn(a, rtInjective(a)) }

  def invertIsInjection[A,B](eqFn: (B,B) => Boolean = defaultEq _)
    (implicit b: Arbitrary[B], bij: Bijection[A, B]) =
      forAll { b: B => eqFn(b, rtInjective(b)(bij.inverse)) }

  def isBijection[A,B](eqFnA: (A,A) => Boolean = defaultEq _, eqFnB: (B,B) => Boolean = defaultEq _)
    (implicit arba: Arbitrary[A], arbb: Arbitrary[B], bij: Bijection[A, B]) =
      isInjection[A,B](eqFnA) && invertIsInjection[A,B](eqFnB)

  def arbitraryViaBijection[A,B](implicit bij: Bijection[A,B], arb: Arbitrary[A]): Arbitrary[B] =
    Arbitrary { arb.arbitrary.map { bij(_) } }
  def arbitraryViaFn[A,B](fn: A => B)(implicit arb: Arbitrary[A]): Arbitrary[B] =
    Arbitrary { arb.arbitrary.map { fn(_) } }
}
