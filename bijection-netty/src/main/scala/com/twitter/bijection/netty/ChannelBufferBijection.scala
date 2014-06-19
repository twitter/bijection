/*
 * Copyright 2013 Twitter Inc.
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

package com.twitter.bijection.netty

import com.twitter.bijection.Bijection

import org.jboss.netty.buffer.{ ChannelBuffer, ChannelBuffers }

/**
 * Reads from the current position to the end into an array without changing
 * the given ChannelBuffer
 */
object ChannelBufferBijection extends Bijection[ChannelBuffer, Array[Byte]] {
  override def apply(cb: ChannelBuffer) = {
    val dup = cb.duplicate
    val result = new Array[Byte](dup.readableBytes)
    dup.readBytes(result)
    result
  }
  override def invert(ary: Array[Byte]) = ChannelBuffers.wrappedBuffer(ary)
}
