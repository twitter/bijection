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
import java.nio.ByteBuffer
import java.util.UUID

import Bijection.{build, fromInjection}

trait NumericBijections extends GeneratedTupleBijections {
  /**
   * Bijections between the numeric types and their java versions.
   */
  implicit val byte2Boxed: Bijection[Byte, JByte] =
    new Bijection[Byte, JByte] {
      def apply(b: Byte) = JByte.valueOf(b)
      override def invert(b: JByte) = b.byteValue
    }

  implicit val short2Boxed: Bijection[Short, JShort] =
    new Bijection[Short, JShort] {
      def apply(s: Short) = JShort.valueOf(s)
      override def invert(s: JShort) = s.shortValue
    }

  implicit val short2ByteByte: Bijection[Short,(Byte,Byte)] =
    new AbstractBijection[Short,(Byte,Byte)] {
      def apply(l: Short) = ((l >>> 8).toByte, l.toByte)
      override def invert(tup: (Byte,Byte)) = {
        val lowbits = tup._2.toInt & 0xff
        (((tup._1 << 8) | lowbits)).toShort
      }
    }

  implicit val int2Boxed: Bijection[Int, JInt] =
    new Bijection[Int, JInt] {
      def apply(i: Int) = JInt.valueOf(i)
      override def invert(i: JInt) =  i.intValue
    }

  implicit val int2ShortShort: Bijection[Int,(Short,Short)] =
    new AbstractBijection[Int,(Short,Short)] {
      def apply(l: Int) = ((l >>> 16).toShort, l.toShort)
      override def invert(tup: (Short,Short)) =
        ((tup._1.toInt << 16) | ((tup._2.toInt << 16) >>> 16))
    }

  implicit val long2Boxed: Bijection[Long, JLong] =
    new Bijection[Long, JLong] {
      def apply(l: Long) = JLong.valueOf(l)
      override def invert(l: JLong) = l.longValue
    }

  implicit val long2IntInt: Bijection[Long,(Int,Int)] =
    new AbstractBijection[Long,(Int,Int)] {
      def apply(l: Long) = ((l >>> 32).toInt, l.toInt)
      override def invert(tup: (Int,Int)) =
        ((tup._1.toLong << 32) | ((tup._2.toLong << 32) >>> 32))
    }

  implicit val float2Boxed: Bijection[Float, JFloat] =
    new Bijection[Float, JFloat] {
      def apply(f: Float) = JFloat.valueOf(f)
      override def invert(f: JFloat) = f.floatValue
    }

  implicit val double2Boxed: Bijection[Double, JDouble] =
    new Bijection[Double, JDouble] {
      def apply(d: Double) = JDouble.valueOf(d)
      override def invert(d: JDouble) = d.doubleValue
    }

  /**
   * Bijections between the numeric types and string.
   */
  implicit val byte2String: Bijection[Byte, String @@ Rep[Byte]] =
    fromInjection[Byte, String]

  implicit val jbyte2String: Bijection[JByte, String @@ Rep[JByte]] =
    fromInjection[JByte, String]

  implicit val short2String: Bijection[Short, String @@ Rep[Short]] =
    fromInjection[Short, String]

  implicit val jshort2String: Bijection[JShort, String @@ Rep[JShort]] =
    fromInjection[JShort, String]

  implicit val int2String: Bijection[Int, String @@ Rep[Int]] =
    fromInjection[Int, String]

  implicit val jint2String: Bijection[JInt, String @@ Rep[JInt]] =
    fromInjection[JInt, String]

  implicit val long2String: Bijection[Long, String @@ Rep[Long]] =
    fromInjection[Long, String]

  implicit val jlong2String: Bijection[JLong, String @@ Rep[JLong]] =
    fromInjection[JLong, String]

  implicit val float2String: Bijection[Float, String @@ Rep[Float]] =
    fromInjection[Float, String]

  implicit val jfloat2String: Bijection[JFloat, String @@ Rep[JFloat]] =
    fromInjection[JFloat, String]

  implicit val double2String: Bijection[Double, String @@ Rep[Double]] =
    fromInjection[Double, String]

  implicit val jdouble2String: Bijection[JDouble, String @@ Rep[JDouble]] =
    fromInjection[JDouble, String]

  val float2IntIEEE754: Bijection[Float, Int] =
    new AbstractBijection[Float, Int] {
      def apply(f: Float) = JFloat.floatToIntBits(f)
      override def invert(i: Int) = JFloat.intBitsToFloat(i)
    }

  val double2LongIEEE754: Bijection[Double, Long] =
    new AbstractBijection[Double, Long] {
      def apply(d: Double) = JDouble.doubleToLongBits(d)
      override def invert(l: Long) = JDouble.longBitsToDouble(l)
    }

  /* Other types to and from Numeric types */
  implicit val uid2LongLong: Bijection[UUID, (Long,Long)] =
    new Bijection[UUID, (Long,Long)] { uid =>
      def apply(uid: UUID) =
        (uid.getMostSignificantBits, uid.getLeastSignificantBits)
      override def invert(ml: (Long,Long)) = new UUID(ml._1, ml._2)
    }

  implicit val date2Long: Bijection[java.util.Date, Long] =
    new Bijection[java.util.Date, Long] {
      def apply(d: java.util.Date) = d.getTime
      override def invert(l: Long) = new java.util.Date(l)
    }
}
