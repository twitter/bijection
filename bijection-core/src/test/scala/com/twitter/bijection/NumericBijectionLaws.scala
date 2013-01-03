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

import java.util.UUID

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

import Bijection.biject // get the .as syntax

object NumericBijectionLaws extends Properties("NumericBijections")
with BaseProperties {
  property("round trips byte -> jbyte") = roundTrips[Byte, JByte]()
  property("round trips short -> jshort") = roundTrips[Short, JShort]()
  property("round trips int -> jint") = roundTrips[Int, JInt]()
  property("round trips long -> jlong") = roundTrips[Long, JLong]()
  property("round trips float -> jfloat") = roundTrips[Float, JFloat]()
  property("round trips double -> jdouble") = roundTrips[Double, JDouble]()

  property("round trips byte -> string") = roundTrips[Byte, String]()
  property("round trips short -> string") = roundTrips[Short, String]()
  property("round trips int -> string") = roundTrips[Int, String]()
  property("round trips long -> string") = roundTrips[Long, String]()
  property("round trips float -> string") = roundTrips[Float, String]()
  property("round trips double -> string") = roundTrips[Double, String]()

  property("round trips short -> Array[Byte]") = roundTrips[Short, Array[Byte]]()
  property("round trips int -> Array[Byte]") = roundTrips[Int, Array[Byte]]()
  property("round trips long -> Array[Byte]") = roundTrips[Long, Array[Byte]]()
  property("round trips float -> Array[Byte]") = roundTrips[Float, Array[Byte]]()
  property("round trips double -> Array[Byte]") = roundTrips[Double, Array[Byte]]()
  // Some other types through numbers:
  property("round trips (Long,Long) -> UUID") = roundTrips[(Long,Long), UUID]()
  property("round trips Long -> Date") = roundTrips[Long, java.util.Date]()

  property("as works") = forAll { (i: Int) =>
    i.as[String] == i.toString && (i.toString.as[Int] == i)
  }
}
