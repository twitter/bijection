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
import org.scalacheck.Prop._

object StringBijectionLaws extends Properties("StringBijections")
with BaseProperties {
  implicit val bij: Bijection[String, Array[Byte]] = StringCodec.utf8

  property("round trips string -> Array[Byte]") = roundTrips[String, Array[Byte]]()
  property("rts through StringJoinBijection") =
    forAll { (sep: String, xs: List[String]) =>
      val sjBij = StringJoinBijection(sep)
      val iter = xs.toIterable
      (!iter.exists(_.contains(sep))) ==> (iter == rt(iter)(sjBij))
    }
}
