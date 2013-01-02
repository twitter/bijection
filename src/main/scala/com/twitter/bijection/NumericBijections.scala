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

trait NumericBijections {
  /**
   * Bijections between the numeric types and their java versions.
   */
  implicit val byte2Boxed: Bijection[Byte, JByte] =
    Bijection[Byte, JByte] { JByte.valueOf(_) } { _.byteValue }
  implicit val short2Boxed: Bijection[Short, JShort] =
    Bijection[Short, JShort] { JShort.valueOf(_) } { _.shortValue }
  implicit val int2Boxed: Bijection[Int, JInt] =
    Bijection[Int, JInt] { JInt.valueOf(_) } { _.intValue }
  implicit val long2Boxed: Bijection[Long, JLong] =
    Bijection[Long, JLong] { JLong.valueOf(_) } { _.longValue }
  implicit val float2Boxed: Bijection[Float, JFloat] =
    Bijection[Float, JFloat] { JFloat.valueOf(_) } { _.floatValue }
  implicit val double2Boxed: Bijection[Double, JDouble] =
    Bijection[Double, JDouble] { JDouble.valueOf(_) } { _.doubleValue }

  /**
   * Bijections between the numeric types and string.
   */
  implicit val byte2String: Bijection[Byte, String] =
    Bijection[Byte, String] { _.toString } { _.toByte }
  implicit val short2String: Bijection[Short, String] =
    Bijection[Short, String] { _.toString } { _.toShort }
  implicit val int2String: Bijection[Int, String] =
    Bijection[Int, String] { _.toString } { _.toInt }
  implicit val long2String: Bijection[Long, String] =
    Bijection[Long, String] { _.toString } { _.toLong }
  implicit val float2String: Bijection[Float, String] =
    Bijection[Float, String] { _.toString } { _.toFloat }
  implicit val double2String: Bijection[Double, String] =
    Bijection[Double, String] { _.toString } { _.toDouble }

  /**
   * Bijections between the numeric types and Array[Byte].
   */
  val float2IntIEEE754: Bijection[Float, Int] =
    Bijection[Float, Int] { JFloat.floatToIntBits(_) } { JFloat.intBitsToFloat(_) }
  val double2LongIEEE754: Bijection[Double, Long] =
    Bijection[Double, Long] { JDouble.doubleToLongBits(_) } { JDouble.longBitsToDouble(_) }

  implicit val short2BigEndian: Bijection[Short, Array[Byte]] =
    Bijection[Short, Array[Byte]] { value =>
      val buf = ByteBuffer.allocate(2)
      buf.putShort(value)
      buf.array
    } { ByteBuffer.wrap(_).getShort }
  implicit val int2BigEndian: Bijection[Int, Array[Byte]] =
    Bijection[Int, Array[Byte]] { value =>
      val buf = ByteBuffer.allocate(4)
      buf.putInt(value)
      buf.array
    } { ByteBuffer.wrap(_).getInt }
  implicit val long2BigEndian: Bijection[Long, Array[Byte]] =
    Bijection[Long, Array[Byte]] { value =>
      val buf = ByteBuffer.allocate(8)
      buf.putLong(value)
      buf.array
    } { ByteBuffer.wrap(_).getLong }
  implicit val float2BigEndian: Bijection[Float, Array[Byte]] =
    float2IntIEEE754 andThen int2BigEndian
  implicit val double2BigEndian: Bijection[Double, Array[Byte]] =
    double2LongIEEE754 andThen long2BigEndian
}
