/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.twitter.bijection.hbase

import com.twitter.bijection._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import scala.annotation.implicitNotFound

/**
 * Provides various HBase specific Bijections by wrapping org.apache.hadoop.hbase.util.Bytes
 * @author Muhammad Ashraf
 * @since 7/9/13
 */
object HBaseBijections {
  /** Byte Representation of a String value */
  type StringBytes = Array[Byte] @@ Rep[String]

  /** Byte Representation of a Long value */
  type LongBytes = Array[Byte] @@ Rep[Long]

  /** Byte Representation of a Boolean value */
  type BooleanBytes = Array[Byte] @@ Rep[Boolean]

  /** Byte Representation of a Int value */
  type IntBytes = Array[Byte] @@ Rep[Int]

  /** Byte Representation of a Double value */
  type DoubleBytes = Array[Byte] @@ Rep[Double]

  /** Byte Representation of a Float value */
  type FloatBytes = Array[Byte] @@ Rep[Float]

  /** Byte Representation of a Short value */
  type ShortBytes = Array[Byte] @@ Rep[Short]

  /** Byte Representation of a BigDecimal value */
  type BigDecimalBytes = Array[Byte] @@ Rep[BigDecimal]


  implicit lazy val string2Bytes: Bijection[String, StringBytes] =
    new AbstractBijection[String, StringBytes] {
      def apply(str: String) = Tag[Array[Byte], Rep[String]](Bytes.toBytes(str))

      override def invert(bytes: StringBytes) = Bytes.toString(bytes)
    }

  implicit lazy val long2Bytes: Bijection[Long, LongBytes] =
    new AbstractBijection[Long, LongBytes] {
      def apply(input: Long) = Tag[Array[Byte], Rep[Long]](Bytes.toBytes(input))

      override def invert(bytes: LongBytes) = Bytes.toLong(bytes)
    }

  implicit lazy val boolean2Bytes: Bijection[Boolean, BooleanBytes] =
    new AbstractBijection[Boolean, BooleanBytes] {
      def apply(input: Boolean) = Tag[Array[Byte], Rep[Boolean]](Bytes.toBytes(input))

      override def invert(bytes: BooleanBytes) = Bytes.toBoolean(bytes)
    }

  implicit lazy val int2Bytes: Bijection[Int, IntBytes] =
    new AbstractBijection[Int, IntBytes] {
      def apply(input: Int) = Tag[Array[Byte], Rep[Int]](Bytes.toBytes(input))

      override def invert(bytes: IntBytes) = Bytes.toInt(bytes)
    }

  implicit lazy val float2Bytes: Bijection[Float, FloatBytes] =
    new AbstractBijection[Float, FloatBytes] {
      def apply(input: Float) = Tag[Array[Byte], Rep[Float]](Bytes.toBytes(input))

      override def invert(bytes: FloatBytes) = Bytes.toFloat(bytes)
    }

  implicit lazy val short2Bytes: Bijection[Short, ShortBytes] =
    new AbstractBijection[Short, ShortBytes] {
      def apply(input: Short) = Tag[Array[Byte], Rep[Short]](Bytes.toBytes(input))

      override def invert(bytes: ShortBytes) = Bytes.toShort(bytes)
    }

  implicit lazy val double2Bytes: Bijection[Double, DoubleBytes] =
    new AbstractBijection[Double, DoubleBytes] {
      def apply(input: Double) = Tag[Array[Byte], Rep[Double]](Bytes.toBytes(input))

      override def invert(bytes: DoubleBytes) = Bytes.toDouble(bytes)
    }

  implicit lazy val bigdecimal2Bytes: Bijection[BigDecimal, BigDecimalBytes] =
    new AbstractBijection[BigDecimal, BigDecimalBytes] {
      def apply(input: BigDecimal) = Tag[Array[Byte], Rep[BigDecimal]](Bytes.toBytes(input.underlying()))

      override def invert(bytes: BigDecimalBytes) = Bytes.toBigDecimal(bytes)
    }

  implicit lazy val string2BytesWritable = ImmutableBytesWritableBijection[String]
  implicit lazy val int2BytesWritable = ImmutableBytesWritableBijection[Int]
  implicit lazy val long2BytesWritable = ImmutableBytesWritableBijection[Long]
  implicit lazy val double2BytesWritable = ImmutableBytesWritableBijection[Double]
  implicit lazy val float2BytesWritable = ImmutableBytesWritableBijection[Float]
  implicit lazy val short2BytesWritable = ImmutableBytesWritableBijection[Short]
  implicit lazy val boolean2BytesWritable = ImmutableBytesWritableBijection[Boolean]
  implicit lazy val bigDecimal2BytesWritable = ImmutableBytesWritableBijection[BigDecimal]
  implicit lazy val bytes2BytesWritable = new AbstractBijection[Array[Byte], ImmutableBytesWritable] {
    override def apply(a: Array[Byte]): ImmutableBytesWritable = new ImmutableBytesWritable(a)

    override def invert(b: ImmutableBytesWritable): Array[Byte] = b.get()
  }

  object ImmutableBytesWritableBijection {
    def apply[T](implicit bijection: Bijection[T, Array[Byte] @@ Rep[T]]) = new ImmutableBytesWritableBijection[T](bijection)
  }

  class ImmutableBytesWritableBijection[@specialized T](bijection: Bijection[T, Array[Byte] @@ Rep[T]]) extends Bijection[T, ImmutableBytesWritable] {
    def apply(a: T): ImmutableBytesWritable = new ImmutableBytesWritable(bijection(a))

    override def invert(b: ImmutableBytesWritable): T = bijection.invert(Tag[Array[Byte], Rep[T]](b.get()))
  }

}
