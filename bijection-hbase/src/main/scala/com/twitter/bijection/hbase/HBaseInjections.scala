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

import com.twitter.bijection.AbstractInjection
import com.twitter.bijection.Injection
import com.twitter.bijection.Tag
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.util.Bytes
import scala.util.Try
import scala.util.Success

/**
 * Provides various HBase specific Injections by wrapping org.apache.hadoop.hbase.util.Bytes
 * @author Mansur Ashraf
 * @since 9/10/13
 */
object HBaseInjections {
  import Injection.buildCatchInvert

  // Array[Byte] injections
  implicit lazy val string2BytesInj: Injection[String, Array[Byte]] =
    buildCatchInvert[String, Array[Byte]](Bytes.toBytes)(Bytes.toString)
  implicit lazy val long2BytesInj: Injection[Long, Array[Byte]] =
    buildCatchInvert[Long, Array[Byte]](Bytes.toBytes)(Bytes.toLong)
  implicit lazy val boolean2BytesInj: Injection[Boolean, Array[Byte]] =
    buildCatchInvert[Boolean, Array[Byte]](Bytes.toBytes)(Bytes.toBoolean)
  implicit lazy val int2BytesInj: Injection[Int, Array[Byte]] =
    buildCatchInvert[Int, Array[Byte]](Bytes.toBytes)(Bytes.toInt)
  implicit lazy val float2BytesInj: Injection[Float, Array[Byte]] =
    buildCatchInvert[Float, Array[Byte]](Bytes.toBytes)(Bytes.toFloat)
  implicit lazy val short2BytesInj: Injection[Short, Array[Byte]] =
    buildCatchInvert[Short, Array[Byte]](Bytes.toBytes)(Bytes.toShort)
  implicit lazy val double2BytesInj: Injection[Double, Array[Byte]] =
    buildCatchInvert[Double, Array[Byte]](Bytes.toBytes)(Bytes.toDouble)
  implicit lazy val bigDecimal2BytesInj: Injection[BigDecimal, Array[Byte]] =
    new AbstractInjection[BigDecimal, Array[Byte]] {
      override def apply(a: BigDecimal) = Bytes.toBytes(a.underlying)
      override def invert(b: Array[Byte]) = Try(Bytes.toBigDecimal(b)).map(BigDecimal(_))
    }

  /**
   * ImmutableBytesWritable injections avoid copying when possible and use the
   * slice (offset and length) of the underlying byte array.
   */
  implicit lazy val string2BytesWritableInj =
    new ImmutableBytesWritableInjection[String] {
      override def invert(b: ImmutableBytesWritable) =
        Try {
          val str = Bytes.toString(b.get, b.getOffset, b.getLength)
          if (str == null) sys.error("Invalid string")
          else str
        }
    }
  implicit lazy val int2BytesWritableInj =
    new ImmutableBytesWritableInjection[Int] {
      override def invert(b: ImmutableBytesWritable) = Try {
        Bytes.toInt(b.get, b.getOffset, b.getLength)
      }
    }
  implicit lazy val long2BytesWritableInj =
    new ImmutableBytesWritableInjection[Long] {
      override def invert(b: ImmutableBytesWritable) = Try {
        Bytes.toLong(b.get, b.getOffset, b.getLength)
      }
    }
  implicit lazy val double2BytesWritableInj =
    new ImmutableBytesWritableInjection[Double] {
      override def invert(b: ImmutableBytesWritable) = Try {
        Bytes.toDouble(b.get, b.getOffset)
      }
    }
  implicit lazy val float2BytesWritableInj =
    new ImmutableBytesWritableInjection[Float] {
      override def invert(b: ImmutableBytesWritable) = Try {
        Bytes.toFloat(b.get, b.getOffset)
      }
    }
  implicit lazy val short2BytesWritableInj =
    new ImmutableBytesWritableInjection[Short] {
      override def invert(b: ImmutableBytesWritable) = Try {
        Bytes.toShort(b.get, b.getOffset, b.getLength)
      }
    }
  implicit lazy val boolean2BytesWritableInj =
    new ImmutableBytesWritableInjection[Boolean] {
      override def invert(b: ImmutableBytesWritable) = Try {
        Bytes.toBoolean(b.copyBytes)
      }
    }
  implicit lazy val bigDecimal2BytesWritableInj =
    new ImmutableBytesWritableInjection[BigDecimal] {
      override def invert(b: ImmutableBytesWritable) = Try {
        Bytes.toBigDecimal(b.get, b.getOffset, b.getLength)
      }
    }
  implicit lazy val bytes2BytesWritableInj: Injection[Array[Byte], ImmutableBytesWritable] =
    new AbstractInjection[Array[Byte], ImmutableBytesWritable] {
      override def apply(a: Array[Byte]) = new ImmutableBytesWritable(a)
      override def invert(b: ImmutableBytesWritable) = Try(b.copyBytes)
    }

  abstract class ImmutableBytesWritableInjection[T](implicit inj: Injection[T, Array[Byte]]) extends AbstractInjection[T, ImmutableBytesWritable] {
    override def apply(a: T): ImmutableBytesWritable = new ImmutableBytesWritable(inj(a))
  }
}
