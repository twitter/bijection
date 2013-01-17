/*
 * Copyright 2010 Twitter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.bijection.scrooge

import com.twitter.bijection.Bijection
import com.twitter.scrooge.{
  BinaryThriftStructSerializer,
  CompactThriftSerializer,
  JsonThriftSerializer,
  ThriftEnum,
  ThriftStruct,
  ThriftStructCodec,
  ThriftStructSerializer
}

class ScroogeScalaCodec[T <: ThriftStruct](ser: ThriftStructSerializer[T])
    extends Bijection[T, Array[Byte]] {
  override def apply(item: T) = ser.toBytes(item)
  override def invert(bytes: Array[Byte]) = ser.fromBytes(bytes)
}

object BinaryScroogeScalaCodec {
  def apply[T <: ThriftStruct](c: ThriftStructCodec[T]) =
    new BinaryScroogeScalaCodec[T](c)
}
class BinaryScroogeScalaCodec[T <: ThriftStruct](c: ThriftStructCodec[T])
    extends ScroogeScalaCodec(new BinaryThriftStructSerializer[T] {
      override def codec = c
    }
  )

object CompactScroogeScalaCodec {
  def apply[T <: ThriftStruct](c: ThriftStructCodec[T]) =
    new CompactScroogeScalaCodec[T](c)
}
class CompactScroogeScalaCodec[T <: ThriftStruct](c: ThriftStructCodec[T])
    extends ScroogeScalaCodec(new CompactThriftSerializer[T] {
      override def codec = c
    }
  )

// TODO: add JSON and ThriftEnum codecs
