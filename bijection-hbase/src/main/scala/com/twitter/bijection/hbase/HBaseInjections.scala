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

  implicit lazy val string2BytesInj = fromBijectionRep[String, StringBytes]
  implicit lazy val long2BytesInj = fromBijectionRep[Long, LongBytes]
  implicit lazy val boolean2BytesInj = fromBijectionRep[Boolean, BooleanBytes]
  implicit lazy val int2BytesInj = fromBijectionRep[Int, IntBytes]
  implicit lazy val float2BytesInj = fromBijectionRep[Float, FloatBytes]
  implicit lazy val short2BytesInj = fromBijectionRep[Short, ShortBytes]
  implicit lazy val double2BytesInj = fromBijectionRep[Double, DoubleBytes]
  implicit lazy val bigdecimal2BytesInj = fromBijectionRep[BigDecimal, BigDecimalBytes]
  implicit lazy val string2BytesWritableInj = fromBijectionRep[String, ImmutableBytesWritable]
  implicit lazy val int2BytesWritableInj = fromBijectionRep[Int, ImmutableBytesWritable]
  implicit lazy val long2BytesWritableInj = fromBijectionRep[Long, ImmutableBytesWritable]
  implicit lazy val double2BytesWritableInj = fromBijectionRep[Double, ImmutableBytesWritable]
  implicit lazy val float2BytesWritableInj = fromBijectionRep[Float, ImmutableBytesWritable]
  implicit lazy val short2BytesWritableInj = fromBijectionRep[Short, ImmutableBytesWritable]
  implicit lazy val boolean2BytesWritableInj = fromBijectionRep[Boolean, ImmutableBytesWritable]
  implicit lazy val bigDecimal2BytesWritableInj = fromBijectionRep[BigDecimal, ImmutableBytesWritable]
}
