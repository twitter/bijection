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

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Arbitrary

import com.twitter.bijection.{ CheckProperties, BaseProperties, Injection }
import org.apache.hadoop.hbase.io.ImmutableBytesWritable

object HBaseInjectionsLaws {
  implicit val arbitaryImmutableBytesWritable: Arbitrary[ImmutableBytesWritable] =
    Arbitrary(for {
      arbByteArray <- Arbitrary.arbitrary[Array[Byte]]
    } yield new ImmutableBytesWritable(arbByteArray))

  implicit val equivImmutableBytesWritable = new Equiv[ImmutableBytesWritable] {
    def equiv(x: ImmutableBytesWritable, y: ImmutableBytesWritable): Boolean = x == y
  }
}

/**
 * @author Muhammad Ashraf
 * @since 7/10/13
 */
class HBaseInjectionsLaws extends CheckProperties with BaseProperties {
  import HBaseInjections._

  import HBaseInjectionsLaws.arbitaryImmutableBytesWritable
  import HBaseInjectionsLaws.equivImmutableBytesWritable

  property("String -> Array[Byte]") {
    isLooseInjection[String, Array[Byte]]
  }

  property("Int -> Array[Byte]") {
    isLooseInjection[Int, Array[Byte]]
  }

  property("Long -> Array[Byte]") {
    isLooseInjection[Long, Array[Byte]]
  }

  property("Double -> Array[Byte]") {
    isLooseInjection[Double, Array[Byte]]
  }

  property("Float -> Array[Byte]") {
    isLooseInjection[Float, Array[Byte]]
  }

  property("Boolean -> Array[Byte]") {
    isLooseInjection[Boolean, Array[Byte]]
  }

  property("Short -> Array[Byte]") {
    isLooseInjection[Short, Array[Byte]]
  }

  property("String -> ImmutableBytesWritable") {
    isLooseInjection[String, ImmutableBytesWritable]
  }

  property("Int -> ImmutableBytesWritable") {
    isLooseInjection[Int, ImmutableBytesWritable]
  }

  property("Long -> ImmutableBytesWritable") {
    isLooseInjection[Long, ImmutableBytesWritable]
  }

  property("Double -> ImmutableBytesWritable") {
    isLooseInjection[Double, ImmutableBytesWritable]
  }

  property("Float -> ImmutableBytesWritable") {
    isLooseInjection[Float, ImmutableBytesWritable]
  }

  property("Boolean -> ImmutableBytesWritable") {
    isLooseInjection[Boolean, ImmutableBytesWritable]
  }

  property("Short -> ImmutableBytesWritable") {
    isLooseInjection[Short, ImmutableBytesWritable]
  }

  property("Array[Byte] -> ImmutableBytesWritable") {
    isLooseInjection[Array[Byte], ImmutableBytesWritable]
  }
}
