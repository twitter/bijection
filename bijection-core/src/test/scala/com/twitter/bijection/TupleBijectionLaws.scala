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

import java.lang.{
  Short => JShort,
  Integer => JInt,
  Long => JLong,
  Float => JFloat,
  Double => JDouble,
  Byte => JByte
}

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object TupleBijectionLaws extends Properties("TupleBijections")
with BaseProperties {
  property("round trips (Int,Long) -> (String,String)") = roundTrips[(Int,Long), (String @@ Rep[Int],String @@ Rep[Long])]()
  property("round trips (Int,Long,String) -> (String,String,String)") =
    roundTrips[(Int,Long,String), (String @@ Rep[Int],String @@ Rep[Long],String)]()
  property("round trips (Int,Long,String,Long) -> (String,String,String,Array[Byte])") =
    roundTrips[(Int,Long,String,Long), (String @@ Rep[Int],String @@ Rep[Long],String,Array[Byte])]()
}
