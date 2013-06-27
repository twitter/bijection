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

import org.specs._

class AsSyntax extends Specification {
  noDetailedDiffs()

  import Conversion.asMethod

  "As syntax" should {
    "work on injections" in {
      val listAry = (1,2).as[List[Array[Byte]]]
      Injection.connect[(Int,Int), List[Array[Byte]]].invert(listAry) must be_==(Right(1->2))
    }
    "work on bijections" in {
      List(1,2,3).as[Vector[Int]] must be_==(Vector(1,2,3))
    }
    "work on functions" in {
      implicit def toS(i: Int): String = i.toString
      23.as[String] must be_==("23")
    }
  }

}
