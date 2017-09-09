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

import java.math.BigInteger
import java.util.UUID

import org.scalacheck.{ Gen, Prop }

import org.scalatest.{PropSpec, Tag => stTag}
import org.scalatest.prop.{ Checkers, PropertyChecks }
import Checkers.check
import PropertyChecks.forAll

import Conversion.asMethod // get the .as syntax

object JavaNumArbs {
  import BaseProperties._

  implicit val byteA = arbitraryViaFn { v: Byte =>
    JByte.valueOf(v)
  }
  implicit val shortA = arbitraryViaFn { v: Short =>
    JShort.valueOf(v)
  }
  implicit val longA = arbitraryViaFn { v: Long =>
    JLong.valueOf(v)
  }
  implicit val intA = arbitraryViaFn { v: Int =>
    JInt.valueOf(v)
  }
  implicit val floatA = arbitraryViaFn { v: Float =>
    JFloat.valueOf(v)
  }
  implicit val doubleA = arbitraryViaFn { v: Double =>
    JDouble.valueOf(v)
  }
  implicit val bigInteger = arbitraryViaFn { (l1l2: (Long, Long)) =>
    (new BigInteger(l1l2._1.toString)).multiply(new BigInteger(l1l2._2.toString))
  }
}

class NumericBijectionLaws extends PropSpec {
  import StringArbs._
  import JavaNumArbs._
  import BaseProperties._

  property("round trips byte -> jbyte") {
    check(isBijection[Byte, JByte])
  }

  property("round trips short -> jshort") {
    check(isBijection[Short, JShort])
  }

  property("round trips int -> jint") {
    check(isBijection[Int, JInt])
  }

  property("round trips long -> jlong") {
    check(isBijection[Long, JLong])
  }

  property("round trips float -> jfloat") {
    check(isBijection[Float, JFloat])
  }

  property("round trips double -> jdouble") {
    check(isBijection[Double, JDouble])
  }

  property("round trips BigInt <-> java.math.BigInteger") {
    check(isBijection[BigInt, BigInteger])
  }

  property("round trips byte -> string") {
    check(isBijection[Byte, String @@ Rep[Byte]])
  }

  property("round trips short -> string") {
    check(isBijection[Short, String @@ Rep[Short]])
  }

  property("round trips int -> string") {
    check(isBijection[Int, String @@ Rep[Int]])
  }

  property("round trips long -> string") {
    check(isBijection[Long, String @@ Rep[Long]])
  }

  property("round trips float -> string") {
    check(isBijection[Float, String @@ Rep[Float]])
  }

  property("round trips double -> string") {
    check(isBijection[Double, String @@ Rep[Double]])
  }

  // Embedding in larger numbers:
  property("round trips Short <-> (Byte,Byte)") {
    check(isBijection[Short, (Byte, Byte)])
  }

  property("round trips Int <-> (Short,Short)") {
    check(isBijection[Int, (Short, Short)])
  }

  property("round trips Long <-> (Int,Int)") {
    check(isBijection[Long, (Int, Int)])
  }

  // Upcasting:
  property("byte -> short") {
    check(isInjection[Byte, Short])
  }

  property("short -> int") {
    check(isInjection[Short, Int])
  }

  property("int -> long") {
    check(isInjection[Int, Long])
  }

  property("long -> BigInt") {
    check(isInjection[Long, BigInt])
  }

  property("int -> double") {
    check(isLooseInjection[Int, Double])
  }

  property("float -> double") {
    check(isLooseInjection[Float, Double])
  }

  // ModDiv
  property("Int -> (Int,Int) by ModDiv") {
    forAll(Gen.choose(1, Int.MaxValue)) { mod =>
      implicit val modDiv: Injection[Int, (Int, Int)] = new IntModDivInjection(mod)
      isInjection[Int, (Int, Int)]
    }
  }

  property("Long -> (Long,Long) by ModDiv") {
    forAll(Gen.choose(1L, Long.MaxValue)) { mod =>
      implicit val modDiv: Injection[Long, (Long, Long)] = new LongModDivInjection(mod)
      isInjection[Long, (Long, Long)]
    }
  }

  // TODO need Rep[Int], etc... on the Array[Byte]
  property("round trips short -> Array[Byte]") {
    isLooseInjection[Short, Array[Byte]]
  }

  property("round trips int -> Array[Byte]") {
    isLooseInjection[Int, Array[Byte]]
  }

  property("round trips long -> Array[Byte]") {
    isLooseInjection[Long, Array[Byte]]
  }

  property("round trips float -> Array[Byte]") {
    isLooseInjection[Float, Array[Byte]]
  }

  property("round trips double -> Array[Byte]") {
    isLooseInjection[Double, Array[Byte]]
  }

  // Some other types through numbers:
  implicit val uuid = arbitraryViaFn { (uplow: (Long, Long)) =>
    new UUID(uplow._1, uplow._2)
  }
  implicit val date = arbitraryViaFn { (dtime: Long) =>
    new java.util.Date(dtime)
  }
  property("round trips (Long,Long) -> UUID") {
    isBijection[(Long, Long), UUID]
  }

  property("round trips Long -> Date") {
    isBijection[Long, java.util.Date]
  }

  property("as works") {
    forAll { (i: Int) =>
      assert(
        (i.as[String @@ Rep[Int]] == i.toString) && (Tag[String, Rep[Int]](i.toString)
          .as[Int] == i))
    }
  }
}
