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
import scala.util.Success
import Inversion.{attempt, attemptWhen}

trait NumericInjections extends GeneratedTupleInjections {
  implicit val byte2Short: Injection[Byte, Short] = new AbstractInjection[Byte, Short] {
    def apply(i: Byte) = i.toShort
    def invert(l: Short) =
      attemptWhen(l)(_.isValidByte)(_.toByte)
  }
  implicit val short2Int: Injection[Short, Int] = new AbstractInjection[Short, Int] {
    def apply(i: Short) = i.toInt
    def invert(l: Int) =
      attemptWhen(l)(_.isValidShort)(_.toShort)
  }
  implicit val int2Long: Injection[Int, Long] = new AbstractInjection[Int, Long] {
    def apply(i: Int) = i.toLong
    def invert(l: Long) =
      attemptWhen(l)(_.isValidInt)(_.toInt)
  }
  implicit val long2BigInt: Injection[Long, BigInt] = new AbstractInjection[Long, BigInt] {
    def apply(l: Long) = BigInt(l)
    def invert(bi: BigInt) =
      attemptWhen(bi)(bi => bi <= Long.MaxValue && Long.MinValue <= bi)(_.toLong)
  }

  // This is a loose injection
  implicit val float2Double: Injection[Float, Double] = new AbstractInjection[Float, Double] {
    def apply(i: Float) = i.toDouble
    def invert(l: Double) =
      attemptWhen(l)(l => l <= Float.MaxValue && l >= Float.MinValue)(_.toFloat)
  }
  // This is a loose injection
  implicit val int2Double: Injection[Int, Double] = new AbstractInjection[Int, Double] {
    def apply(i: Int) = i.toDouble
    def invert(l: Double) = Success(l.toInt)
  }

  implicit val byte2String: Injection[Byte, String] =
    new AbstractInjection[Byte, String] {
      def apply(b: Byte) = b.toString
      override def invert(s: String) = attempt(s)(_.toByte)
    }
  implicit val jbyte2String: Injection[JByte, String] =
    new AbstractInjection[JByte, String] {
      def apply(b: JByte) = b.toString
      override def invert(s: String) = attempt(s)(JByte.valueOf(_))
    }
  implicit val short2String: Injection[Short, String] =
    new AbstractInjection[Short, String] {
      def apply(s: Short) = s.toString
      override def invert(s: String) = attempt(s)(_.toShort)
    }
  implicit val jshort2String: Injection[JShort, String] =
    new AbstractInjection[JShort, String] {
      def apply(b: JShort) = b.toString
      override def invert(s: String) = attempt(s)(JShort.valueOf(_))
    }
  implicit val int2String: Injection[Int, String] =
    new AbstractInjection[Int, String] {
      def apply(i: Int) = i.toString
      override def invert(s: String) = attempt(s)(_.toInt)
    }
  implicit val jint2String: Injection[JInt, String] =
    new AbstractInjection[JInt, String] {
      def apply(i: JInt) = i.toString
      override def invert(s: String) = attempt(s)(JInt.valueOf(_))
    }

  implicit val long2String: Injection[Long, String] =
    new AbstractInjection[Long, String] {
      def apply(l: Long) = l.toString
      override def invert(s: String) = attempt(s)(_.toLong)
    }
  implicit val jlong2String: Injection[JLong, String] =
    new AbstractInjection[JLong, String] {
      def apply(l: JLong) = l.toString
      override def invert(s: String) = attempt(s)(JLong.valueOf(_))
    }

  implicit val float2String: Injection[Float, String] =
    new AbstractInjection[Float, String] {
      def apply(f: Float) = f.toString
      override def invert(s: String) = attempt(s)(_.toFloat)
    }

  implicit val jfloat2String: Injection[JFloat, String] =
    new AbstractInjection[JFloat, String] {
      def apply(f: JFloat) = f.toString
      override def invert(s: String) = attempt(s)(JFloat.valueOf(_))
    }

  implicit val double2String: Injection[Double, String] =
    new AbstractInjection[Double, String] {
      def apply(d: Double) = d.toString
      override def invert(s: String) = attempt(s)(_.toDouble)
    }

  implicit val jdouble2String: Injection[JDouble, String] =
    new AbstractInjection[JDouble, String] {
      def apply(d: JDouble) = d.toString
      override def invert(s: String) = attempt(s)(JDouble.valueOf(_))
    }

  implicit val short2BigEndian: Injection[Short, Array[Byte]] =
    new AbstractInjection[Short, Array[Byte]] {
      val size = 2
      def apply(value: Short) = {
        val buf = ByteBuffer.allocate(size)
        buf.putShort(value)
        buf.array
      }
      override def invert(b: Array[Byte]) =
        attempt(b)(ByteBuffer.wrap(_).getShort)
    }

  implicit val int2BigEndian: Injection[Int, Array[Byte]] =
    new AbstractInjection[Int, Array[Byte]] { value =>
      val size = 4
      def apply(value: Int) = {
        val buf = ByteBuffer.allocate(size)
        buf.putInt(value)
        buf.array
      }
      override def invert(b: Array[Byte]) =
        attempt(b)(ByteBuffer.wrap(_).getInt)
    }

  implicit val long2BigEndian: Injection[Long, Array[Byte]] =
    new AbstractInjection[Long, Array[Byte]] {
      val size = 8
      def apply(value: Long) = {
        val buf = ByteBuffer.allocate(size)
        buf.putLong(value)
        buf.array
      }
      override def invert(b: Array[Byte]) =
        attempt(b)(ByteBuffer.wrap(_).getLong)
    }

  // Lazy to deal with the fact that int2BigEndian od Bijection may not be init yet
  // there seemed to be some null pointer exceptions in the tests that this fixed
  implicit lazy val float2BigEndian: Injection[Float, Array[Byte]] =
    Injection.fromBijection(Bijection.float2IntIEEE754) andThen int2BigEndian
  implicit lazy val double2BigEndian: Injection[Double, Array[Byte]] =
    Injection.fromBijection(Bijection.double2LongIEEE754) andThen long2BigEndian
}
