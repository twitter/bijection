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

import org.scalacheck.{ Prop, Gen }
import org.scalacheck.Prop.forAll
import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import Conversion.asMethod // get the .as syntax

class EnglishIntLaws extends PropSpec with PropertyChecks with MustMatchers
  with BaseProperties {
  var ct = 0

  def test(x: Gen[Int]) = {
    Prop.forAll(x)({
      i =>
        ct += 1
        i.as[EnglishInt].as[Int] == i
    })
  }

  val (tiny, small, medium, large) = (Gen.choose(0, 100), Gen.choose(100, 1000), Gen.choose(1000, 100 * 1000), Gen.choose(100 * 1000, 1000 * 1000 * 1000))
  property("as works") {
    List(tiny, small, medium, large).map(test).reduceLeft((a, b) => a && b)
  }

}
