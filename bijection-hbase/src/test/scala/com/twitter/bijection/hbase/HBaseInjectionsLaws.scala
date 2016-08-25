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

import org.scalatest.{PropSpec, MustMatchers}
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import com.twitter.bijection.{CheckProperties, BaseProperties, Injection}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable

object HBaseInjectionsLaws {
  implicit val arbitaryImmutableBytesWritable: Arbitrary[ImmutableBytesWritable] =
    Arbitrary(for {
      bytes <- Arbitrary.arbitrary[Array[Byte]]
      offset <- Gen.choose(0, bytes.length - 1)
      len <- Gen.choose(0, bytes.length - offset)
    } yield new ImmutableBytesWritable(bytes, offset, len))
}

/**
  * @author Muhammad Ashraf
  * @since 7/10/13
  */
class HBaseInjectionsLaws extends CheckProperties with BaseProperties {
  import HBaseInjections._
  import HBaseInjectionsLaws.arbitaryImmutableBytesWritable

  property("String -> Array[Byte]") {
    isLooseInjection[String, Array[Byte]]
  }

  property("Int -> Array[Byte]") {
    isInjection[Int, Array[Byte]]
  }

  property("Long -> Array[Byte]") {
    isInjection[Long, Array[Byte]]
  }

  property("Double -> Array[Byte]") {
    isInjection[Double, Array[Byte]]
  }

  property("Float -> Array[Byte]") {
    isInjection[Float, Array[Byte]]
  }

  property("Boolean -> Array[Byte]") {
    isLooseInjection[Boolean, Array[Byte]]
  }

  property("Short -> Array[Byte]") {
    isInjection[Short, Array[Byte]]
  }

  property("String -> ImmutableBytesWritable") {
    isLooseInjection[String, ImmutableBytesWritable]
  }

  property("Int -> ImmutableBytesWritable") {
    isInjection[Int, ImmutableBytesWritable]
  }

  property("Long -> ImmutableBytesWritable") {
    isInjection[Long, ImmutableBytesWritable]
  }

  // This injection will invert a ImmutableBytesWritable whose length is less
  // or greater than the size of a Bytes.toDouble because it incorrectly assumes
  // that you give it the correct number of bytes.
  //
  // i.e. If the ImmutableBytesWritable is a slice of the first byte of an
  // underlying 8 byte array, this injection will use all the bytes when
  // inverting.
  property("Double -> ImmutableBytesWritable") {
    isLooseInjection[Double, ImmutableBytesWritable]
  }

  // This injection will invert a ImmutableBytesWritable whose length is less
  // or greater than the size of a Bytes.toFloat because it incorrectly assumes
  // that you give it the correct number of bytes.
  //
  // i.e. If the ImmutableBytesWritable is a slice of the first byte of an
  // underlying 4 byte array, this injection will use all the bytes when
  // inverting.
  property("Float -> ImmutableBytesWritable") {
    isLooseInjection[Float, ImmutableBytesWritable]
  }

  property("Boolean -> ImmutableBytesWritable") {
    isLooseInjection[Boolean, ImmutableBytesWritable]
  }

  property("Short -> ImmutableBytesWritable") {
    isInjection[Short, ImmutableBytesWritable]
  }

  property("Array[Byte] -> ImmutableBytesWritable") {
    isInjection[Array[Byte], ImmutableBytesWritable]
  }
}
