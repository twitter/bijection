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

import org.scalatest._
import com.twitter.bijection.{ Bijection, BaseProperties }
import HBaseBijections._
import org.apache.hadoop.hbase.io.ImmutableBytesWritable

/**
 * @author Muhammad Ashraf
 * @since 7/10/13
 */
object HBaseBijectionsSpecifications extends WordSpec with Matchers with BaseProperties {

  "HBaseBijections" should {
    "round trip String -> Array[Byte]" in {
      val expected = "Bonjour le monde"
      val bytes = Bijection[String, StringBytes](expected)
      val result = Bijection.invert[String, StringBytes](bytes)
      assert(result == expected)
    }

    "round trip Long -> Array[Byte]" in {
      val expected = 42L
      val bytes = Bijection[Long, LongBytes](expected)
      val result = Bijection.invert[Long, LongBytes](bytes)
      assert(result == expected)
    }

    "round trip Int -> Array[Byte]" in {
      val expected = 42
      val bytes = Bijection[Int, IntBytes](expected)
      val result = Bijection.invert[Int, IntBytes](bytes)
      assert(result == expected)
    }

    "round trip Double -> Array[Byte]" in {
      val expected = 42.0
      val bytes = Bijection[Double, DoubleBytes](expected)
      val result = Bijection.invert[Double, DoubleBytes](bytes)
      assert(result == expected)
    }

    "round trip Float -> Array[Byte]" in {
      val expected = 42.0F
      val bytes = Bijection[Float, FloatBytes](expected)
      val result = Bijection.invert[Float, FloatBytes](bytes)
      assert(result == expected)
    }

    "round trip Short -> Array[Byte]" in {
      val expected = 1.toShort
      val bytes = Bijection[Short, ShortBytes](expected)
      val result = Bijection.invert[Short, ShortBytes](bytes)
      assert(result == expected)
    }

    "round trip BigDecimal -> Array[Byte]" in {
      val expected = BigDecimal(1)
      val bytes = Bijection[BigDecimal, BigDecimalBytes](expected)
      val result = Bijection.invert[BigDecimal, BigDecimalBytes](bytes)
      assert(result == expected)
    }

    "round trip Boolean -> Array[Byte]" in {
      val expected = true
      val bytes = Bijection[Boolean, BooleanBytes](expected)
      val result = Bijection.invert[Boolean, BooleanBytes](bytes)
      assert(result == expected)
    }

    "round trip String -> ImmutableBytesWritable" in {
      val expected = "Bonjour le monde"
      val bytes = Bijection[String, ImmutableBytesWritable](expected)
      val result = Bijection.invert[String, ImmutableBytesWritable](bytes)
      assert(result == expected)
    }

    "round trip Long -> ImmutableBytesWritable" in {
      val expected = 1L
      val bytes = Bijection[Long, ImmutableBytesWritable](expected)
      val result = Bijection.invert[Long, ImmutableBytesWritable](bytes)
      assert(result == expected)
    }

    "round trip Int -> ImmutableBytesWritable" in {
      val expected = 42
      val bytes = Bijection[Int, ImmutableBytesWritable](expected)
      val result = Bijection.invert[Int, ImmutableBytesWritable](bytes)
      assert(result == expected)
    }

    "round trip Double -> ImmutableBytesWritable" in {
      val expected = 42.0
      val bytes = Bijection[Double, ImmutableBytesWritable](expected)
      val result = Bijection.invert[Double, ImmutableBytesWritable](bytes)
      assert(result == expected)
    }

    "round trip Float -> ImmutableBytesWritable" in {
      val expected = 42.0F
      val bytes = Bijection[Float, ImmutableBytesWritable](expected)
      val result = Bijection.invert[Float, ImmutableBytesWritable](bytes)
      assert(result == expected)
    }

    "round trip Short -> ImmutableBytesWritable" in {
      val expected = 1.toShort
      val bytes = Bijection[Short, ImmutableBytesWritable](expected)
      val result = Bijection.invert[Short, ImmutableBytesWritable](bytes)
      assert(result == expected)
    }

    "round trip BigDecimal -> ImmutableBytesWritable" in {
      val expected = BigDecimal(1)
      val bytes = Bijection[BigDecimal, ImmutableBytesWritable](expected)
      val result = Bijection.invert[BigDecimal, ImmutableBytesWritable](bytes)
      assert(result == expected)
    }
  }

}
