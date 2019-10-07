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

import scala.util.Success
import com.twitter.bijection.Injection
import com.twitter.bijection.hbase.HBaseInjections._
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.scalatest.WordSpec

/**
  * @author Muhammad Ashraf
  * @since 7/10/13
  */
class HBaseInjectionsSpecifications extends WordSpec {
  "HBaseInjections" should {
    "respect ImmutableBytesWritable offset and length" in {
      val long: Long = 1
      val double: Double = 2
      val int: Int = 3

      val longIBW = long2BytesWritableInj(long)
      val doubleIBW = double2BytesWritableInj(double)
      val intIBW = int2BytesWritableInj(int)

      val allBytes = longIBW.copyBytes ++ doubleIBW.copyBytes ++ intIBW.copyBytes

      val sharedLongIBW = new ImmutableBytesWritable(allBytes, 0, 8)
      val sharedDoubleIBW = new ImmutableBytesWritable(allBytes, 8, 8)
      val sharedIntIBW = new ImmutableBytesWritable(allBytes, 16, 4)

      assert(Success(long) == long2BytesWritableInj.invert(sharedLongIBW))
      assert(Success(double) == double2BytesWritableInj.invert(sharedDoubleIBW))
      assert(Success(int) == int2BytesWritableInj.invert(sharedIntIBW))
    }

    "round trip String -> Array[Byte]" in {
      val expected = "Bonjour le monde"
      val bytes = Injection[String, Array[Byte]](expected)
      val result = Injection.invert[String, Array[Byte]](bytes)
      assert(Success(expected) == result)
    }

    "round trip Long -> Array[Byte]" in {
      val expected = 42L
      val bytes = Injection[Long, Array[Byte]](expected)
      val result = Injection.invert[Long, Array[Byte]](bytes)
      assert(Success(expected) == result)
    }

    "round trip Int -> Array[Byte]" in {
      val expected = 42
      val bytes = Injection[Int, Array[Byte]](expected)
      val result = Injection.invert[Int, Array[Byte]](bytes)
      assert(Success(expected) == result)
    }

    "round trip Double -> Array[Byte]" in {
      val expected = 42.0
      val bytes = Injection[Double, Array[Byte]](expected)
      val result = Injection.invert[Double, Array[Byte]](bytes)
      assert(Success(expected) == result)
    }

    "round trip Float -> Array[Byte]" in {
      val expected = 42.0f
      val bytes = Injection[Float, Array[Byte]](expected)
      val result = Injection.invert[Float, Array[Byte]](bytes)
      assert(Success(expected) == result)
    }

    "round trip Short -> Array[Byte]" in {
      val expected = 1.toShort
      val bytes = Injection[Short, Array[Byte]](expected)
      val result = Injection.invert[Short, Array[Byte]](bytes)
      assert(Success(expected) == result)
    }

    "round trip BigDecimal -> Array[Byte]" in {
      val expected = BigDecimal(1)
      val bytes = Injection[BigDecimal, Array[Byte]](expected)
      val result = Injection.invert[BigDecimal, Array[Byte]](bytes)
      assert(Success(expected) == result)
    }

    "round trip Boolean -> Array[Byte]" in {
      val expected = true
      val bytes = Injection[Boolean, Array[Byte]](expected)
      val result = Injection.invert[Boolean, Array[Byte]](bytes)
      assert(Success(expected) == result)
    }

    "round trip String -> ImmutableBytesWritable" in {
      val expected = "Bonjour le monde"
      val bytes = Injection[String, ImmutableBytesWritable](expected)
      val result = Injection.invert[String, ImmutableBytesWritable](bytes)
      assert(Success(expected) == result)
    }

    "round trip Long -> ImmutableBytesWritable" in {
      val expected = 1L
      val bytes = Injection[Long, ImmutableBytesWritable](expected)
      val result = Injection.invert[Long, ImmutableBytesWritable](bytes)
      assert(Success(expected) == result)
    }

    "round trip Int -> ImmutableBytesWritable" in {
      val expected = 42
      val bytes = Injection[Int, ImmutableBytesWritable](expected)
      val result = Injection.invert[Int, ImmutableBytesWritable](bytes)
      assert(Success(expected) == result)
    }

    "round trip Double -> ImmutableBytesWritable" in {
      val expected = 42.0
      val bytes = Injection[Double, ImmutableBytesWritable](expected)
      val result = Injection.invert[Double, ImmutableBytesWritable](bytes)
      assert(Success(expected) == result)
    }

    "round trip Float -> ImmutableBytesWritable" in {
      val expected = 42.0f
      val bytes = Injection[Float, ImmutableBytesWritable](expected)
      val result = Injection.invert[Float, ImmutableBytesWritable](bytes)
      assert(Success(expected) == result)
    }

    "round trip Short -> ImmutableBytesWritable" in {
      val expected = 1.toShort
      val bytes = Injection[Short, ImmutableBytesWritable](expected)
      val result = Injection.invert[Short, ImmutableBytesWritable](bytes)
      assert(Success(expected) == result)
    }

    "round trip BigDecimal -> ImmutableBytesWritable" in {
      val expected = BigDecimal(1)
      val bytes = Injection[BigDecimal, ImmutableBytesWritable](expected)
      val result = Injection.invert[BigDecimal, ImmutableBytesWritable](bytes)
      assert(Success(expected) == result)
    }

    "round trip Boolean -> ImmutableBytesWritable" in {
      val expected = true
      val bytes = Injection[Boolean, ImmutableBytesWritable](expected)
      val result = Injection.invert[Boolean, ImmutableBytesWritable](bytes)
      assert(Success(expected) == result)
    }
  }
}
