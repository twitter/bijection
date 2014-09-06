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

import org.scalatest._
import scala.util.Success

class AsSyntax extends WordSpec with Matchers {
  import Conversion.asMethod

  "As syntax" should {
    "work on injections" in {
      val listAry = (1, 2).as[List[Array[Byte]]]
      Injection.connect[(Int, Int), List[Array[Byte]]].invert(listAry) should be(Success(1 -> 2))
    }
    "work on bijections" in {
      List(1, 2, 3).as[Vector[Int]] should be (Vector(1, 2, 3))
    }
    "work on functions" in {
      implicit def toS(i: Int): String = i.toString
      23.as[String] should be ("23")
    }
  }

}
