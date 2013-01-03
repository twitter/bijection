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
  def rt[A, B](a: A)(implicit bij: Bijection[A, B]): A = bij.invert(bij(a))
  def defaultEq[A](a1: A, a2: A) = a1 == a2
  def roundTrips[A, B](eqFn: (A, A) => Boolean = defaultEq _)
  (implicit a: Arbitrary[A], bij: Bijection[A, B]) =
    forAll { a: A => eqFn(a, rt(a)) }
}
