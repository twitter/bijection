/*
Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.twitter.bijection

import scala.collection.generic.CanBuildFrom
import scala.util.Try
import java.nio.ByteBuffer

/**
  * Version specific imlementation for Bufferable on scala 2.13+
  */
trait BufferableVersionSpecific {
  self: Bufferable.type =>
  // TODO we could add IntBuffer/FloatBuffer etc.. to have faster implementations Array[Int]
  implicit def array[T](
      implicit buf: Bufferable[T],
      cbf: CanBuildFrom[Nothing, T, Array[T]]
  ): Bufferable[Array[T]] =
    build[Array[T]] { (bb, l) =>
      putCollection(bb, l.toTraversable)
    } { bb =>
      getCollection(bb)
    }

  def collection[C <: Traversable[T], T](
      implicit buf: Bufferable[T],
      cbf: CanBuildFrom[Nothing, T, C]
  ): Bufferable[C] =
    build[C] { (bb, l) =>
      putCollection(bb, l)
    } { bb =>
      getCollection(bb)
    }

  def getCollection[T, C](
      initbb: ByteBuffer
  )(implicit cbf: CanBuildFrom[Nothing, T, C], buf: Bufferable[T]): Try[(ByteBuffer, C)] = Try {
    var bb: ByteBuffer = initbb.duplicate
    val size = bb.getInt
    var idx = 0
    val builder = cbf()
    builder.clear()
    builder.sizeHint(size)
    while (idx < size) {
      val tup = buf.get(bb).get
      bb = tup._1
      builder += tup._2
      idx += 1
    }
    (bb, builder.result)
  }

}
