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

import com.twitter.bijection.{ Bijection, Injection }
import com.twitter.bijection.Inversion.attempt
import com.twitter.scrooge._
import org.apache.thrift.protocol.TJSONProtocol

import scala.util.Try

class ScalaCodec[T <: ThriftStruct](ser: ThriftStructSerializer[T])
  extends Injection[T, Array[Byte]] {
  override def apply(item: T) = ser.toBytes(item)
  override def invert(bytes: Array[Byte]) = attempt(bytes)(ser.fromBytes(_))
}

object BinaryScalaCodec {
  def apply[T <: ThriftStruct](c: ThriftStructCodec[T]) =
    new BinaryScalaCodec[T](c)
}

class BinaryScalaCodec[T <: ThriftStruct](c: ThriftStructCodec[T])
  extends Injection[T, Array[Byte]] {
  import com.twitter.bijection.thrift.{ TArrayByteTransport, TArrayBinaryProtocol }

  lazy val thriftStructSerializer = new ThriftStructSerializer[T] {
    override def codec = c
    val protocolFactory = new TBinaryProtocol.Factory
  }

  override def apply(item: T) = thriftStructSerializer.toBytes(item)
  override def invert(bytes: Array[Byte]) = attempt(bytes){ bytes =>
    c.decode(TArrayBinaryProtocol(TArrayByteTransport(bytes)))
  }
}

object CompactScalaCodec {
  def apply[T <: ThriftStruct](c: ThriftStructCodec[T]) =
    new CompactScalaCodec[T](c)
}

class JsonScalaCodec[T <: ThriftStruct](c: ThriftStructCodec[T])
  extends Injection[T, String] {
  val ser = new ThriftStructSerializer[T] {
    override def codec = c
    override val protocolFactory = new TJSONProtocol.Factory
  }

  override def apply(a: T): String = ser.toString(a)
  override def invert(b: String): Try[T] = attempt(b)(ser.fromString)
}

object JsonScalaCodec {
  def apply[T <: ThriftStruct](c: ThriftStructCodec[T]) =
    new JsonScalaCodec[T](c)
}

class CompactScalaCodec[T <: ThriftStruct](c: ThriftStructCodec[T])
  extends ScalaCodec(new CompactThriftSerializer[T] {
    override def codec = c
  })

// TODO: add  ThriftEnum codecs
