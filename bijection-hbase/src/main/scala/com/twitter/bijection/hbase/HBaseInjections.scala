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

import HBaseBijections._
import com.twitter.bijection._
import Injection._
import org.apache.hadoop.hbase.io.ImmutableBytesWritable

/**
 * @author Mansur Ashraf
 * @since 9/10/13
 */
object HBaseInjections {

  implicit lazy val string2BytesInj: Injection[String, StringBytes] = fromBijectionRep[String, StringBytes]
  implicit lazy val long2BytesInj: Injection[Long, LongBytes] = fromBijectionRep[Long, LongBytes]
  implicit lazy val boolean2BytesInj: Injection[Boolean, BooleanBytes] = fromBijectionRep[Boolean, BooleanBytes]
  implicit lazy val int2BytesInj: Injection[Int, IntBytes] = fromBijectionRep[Int, IntBytes]
  implicit lazy val float2BytesInj: Injection[Float, FloatBytes] = fromBijectionRep[Float, FloatBytes]
  implicit lazy val short2BytesInj: Injection[Short, ShortBytes] = fromBijectionRep[Short, ShortBytes]
  implicit lazy val double2BytesInj: Injection[Double, DoubleBytes] = fromBijectionRep[Double, DoubleBytes]
  implicit lazy val bigdecimal2BytesInj: Injection[BigDecimal, BigDecimalBytes] = fromBijectionRep[BigDecimal, BigDecimalBytes]
  implicit lazy val string2BytesWritableInj: Injection[String, ImmutableBytesWritable] = fromBijectionRep[String, ImmutableBytesWritable]
  implicit lazy val int2BytesWritableInj: Injection[Int, ImmutableBytesWritable] = fromBijectionRep[Int, ImmutableBytesWritable]
  implicit lazy val long2BytesWritableInj: Injection[Long, ImmutableBytesWritable] = fromBijectionRep[Long, ImmutableBytesWritable]
  implicit lazy val double2BytesWritableInj: Injection[Double, ImmutableBytesWritable] = fromBijectionRep[Double, ImmutableBytesWritable]
  implicit lazy val float2BytesWritableInj: Injection[Float, ImmutableBytesWritable] = fromBijectionRep[Float, ImmutableBytesWritable]
  implicit lazy val short2BytesWritableInj: Injection[Short, ImmutableBytesWritable] = fromBijectionRep[Short, ImmutableBytesWritable]
  implicit lazy val boolean2BytesWritableInj: Injection[Boolean, ImmutableBytesWritable] = fromBijectionRep[Boolean, ImmutableBytesWritable]
  implicit lazy val bigDecimal2BytesWritableInj: Injection[BigDecimal, ImmutableBytesWritable] = fromBijectionRep[BigDecimal, ImmutableBytesWritable]
}
