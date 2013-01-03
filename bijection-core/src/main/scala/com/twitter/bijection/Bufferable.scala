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

import java.nio.{ByteBuffer, BufferOverflowException}
import java.nio.channels.Channel
import scala.annotation.implicitNotFound
import scala.annotation.tailrec
import scala.collection.mutable.{Builder, Map => MMap, Set => MSet, Buffer => MBuffer}

/**
 * Bufferable[T] is a typeclass to work with java.nio.ByteBuffer for serialization/bijections to
 * Array[Byte]
 */

@implicitNotFound(msg = "Cannot find Bufferable type class for ${T}")
trait Bufferable[T] extends java.io.Serializable {
  def put(into: ByteBuffer, t: T): ByteBuffer
  def get(from: ByteBuffer): T
}

/* TODO add a lowest priority where T <: java.io.Serializable
trait LowPriorityBufferable {

}*/

object Bufferable extends GeneratedTupleBufferable with java.io.Serializable {
  val DEFAULT_SIZE = 1024
  // Type class methods:
  def put[T](into: ByteBuffer, t: T)(implicit buf: Bufferable[T]): ByteBuffer = buf.put(into, t)
  def get[T](from: ByteBuffer)(implicit buf: Bufferable[T]): T = buf.get(from)
  // get the bytes from a given position to the current position
  def getBytes(from: ByteBuffer, start: Int = 0): Array[Byte] = {
    val fromd = from.duplicate
    // read position:
    val current = fromd.position
    // set the position:
    fromd.position(start)
    val result = Array.ofDim[Byte](current - start)
    fromd.get(result)
    result
  }
  // With Bijections:
  def viaBijection[A,B](implicit buf: Bufferable[B], bij: Bijection[A,B]): Bufferable[A] =
    Bufferable.build[A] { (bb, a) => buf.put(bb, bij(a)) } { bb => bij.invert(buf.get(bb)) }

  def bijectionOf[T](implicit buf: Bufferable[T]): Bijection[T, Array[Byte]] =
    Bijection.build[T, Array[Byte]] { t =>
      getBytes(put(ByteBuffer.allocateDirect(128), t))
    } { bytes => get[T](ByteBuffer.wrap(bytes)) }

  def reallocate(bb: ByteBuffer): ByteBuffer = {
    // Double the buffer, copy the old one, and put:
    val newCapacity = if (bb.capacity > (DEFAULT_SIZE/2)) bb.capacity * 2 else DEFAULT_SIZE
    val newBb = if (bb.isDirect) {
      ByteBuffer.allocateDirect(newCapacity)
    } else {
      ByteBuffer.allocate(newCapacity)
    }
    val tmpBb = bb.duplicate
    val currentPos = tmpBb.position
    tmpBb.position(0)
    tmpBb.limit(currentPos)
    newBb.put(tmpBb)
    newBb
  }

  // This automatically doubles the ByteBuffer if we get a buffer-overflow
  def reallocatingPut(bb: ByteBuffer)(putfn: (ByteBuffer) => ByteBuffer): ByteBuffer = {
    val init = bb.duplicate
    try {
      putfn(init)
    }
    catch {
      case ex: BufferOverflowException => reallocatingPut(reallocate(bb))(putfn)
    }
  }

  def build[T](putfn: (ByteBuffer,T) => ByteBuffer)(getfn: (ByteBuffer) => T):
    Bufferable[T] = new Bufferable[T] {
      override def put(into: ByteBuffer, t: T) = putfn(into,t)
      override def get(from: ByteBuffer) = getfn(from)
    }

  // Primitives:
  implicit val byteBufferable: Bufferable[Byte] =
    build[Byte] { (bb, x) => reallocatingPut(bb){ _.put(x) } } { _.get }
  implicit val charBufferable: Bufferable[Char] =
    build[Char] { (bb, x) => reallocatingPut(bb){ _.putChar(x) } } { _.getChar }
  implicit val shortBufferable: Bufferable[Short] =
    build[Short] { (bb, x) => reallocatingPut(bb){ _.putShort(x) } } { _.getShort }
  implicit val intBufferable: Bufferable[Int] =
    build[Int] { (bb, x) => reallocatingPut(bb){ _.putInt(x) } } { _.getInt }
  implicit val longBufferable: Bufferable[Long] =
    build[Long] { (bb, x) => reallocatingPut(bb){ _.putLong(x) } } { _.getLong }
  implicit val floatBufferable: Bufferable[Float] =
    build[Float] { (bb, x) => reallocatingPut(bb){ _.putFloat(x) } }  { _.getFloat }
  implicit val doubleBufferable: Bufferable[Double] =
    build[Double] { (bb, x) => reallocatingPut(bb){ _.putDouble(x) } }  { _.getDouble }
  // Writes a length prefix, and then the bytes
  implicit val byteArray: Bufferable[Array[Byte]] =
    build[Array[Byte]] { (bb, ary) =>
      val nextBb = reallocatingPut(bb){ _.putInt(ary.size) }
      reallocatingPut(nextBb){ _.put(ary) }
    } { bb =>
      val ary = Array.ofDim[Byte](bb.getInt)
      bb.get(ary)
      ary
    }
  implicit val stringBufferable : Bufferable[String] = viaBijection[String, Array[Byte]]
  implicit val symbolBufferable : Bufferable[Symbol] = viaBijection[Symbol, String]

  // Collections:
  implicit def option[T](implicit buf: Bufferable[T]): Bufferable[Option[T]] =
    build[Option[T]] { (bb, opt) =>
      opt match {
        case None => reallocatingPut(bb) { _.put(0 : Byte) }
        case Some(v) => {
          val nextBb = reallocatingPut(bb) { _.put(1 : Byte) }
          reallocatingPut(nextBb) { buf.put(_, opt.get) }
        }
      }
    } { bb =>
      val byte0 = 0 : Byte
      if(bb.get == byte0) None else Some(buf.get(bb))
    }
  implicit def either[L,R](implicit bufl: Bufferable[L], bufr: Bufferable[R]): Bufferable[Either[L,R]] =
    build[Either[L,R]] { (bb, eith) =>
      eith match {
        case Left(l) => {
          val nextBb = reallocatingPut(bb) { _.put(0: Byte) }
          reallocatingPut(nextBb) { bufl.put(_, l) }
        }
        case Right(r) => {
          val nextBb = reallocatingPut(bb) { _.put(1: Byte) }
          reallocatingPut(nextBb) { bufr.put(_, r) }
        }
      }
    } { bb =>
      val byte0 = 0 : Byte
      if (bb.get == byte0) Left(bufl.get(bb)) else Right(bufr.get(bb))
    }

  def collection[C<:Traversable[T],T](builder: Builder[T,C])(implicit buf: Bufferable[T]):
    Bufferable[C] = build[C] { (bb, l) =>
      val size = l.size
      val nextBb = reallocatingPut(bb){ _.putInt(size) }
      l.foldLeft(nextBb) { (oldbb, t) => reallocatingPut(oldbb) { buf.put(_, t) } }
    } { bb =>
      val size = bb.getInt
      builder.clear()
      (0 until size).foreach { idx => builder += buf.get(bb) }
      builder.result()
    }
  implicit def list[T](implicit buf: Bufferable[T]) = collection[List[T], T](List.newBuilder[T])
  implicit def set[T](implicit buf: Bufferable[T]) = collection[Set[T], T](Set.newBuilder[T])
  implicit def indexedSeq[T](implicit buf: Bufferable[T]) =
    collection[IndexedSeq[T], T](IndexedSeq.newBuilder[T])
  implicit def vector[T](implicit buf: Bufferable[T]) =
    collection[Vector[T], T](Vector.newBuilder[T])
  implicit def map[K,V](implicit bufk: Bufferable[K], bufv: Bufferable[V]) =
    collection[Map[K,V], (K,V)](Map.newBuilder[K,V])
  // Mutable collections
  implicit def mmap[K,V](implicit bufk: Bufferable[K], bufv: Bufferable[V]) =
    collection[MMap[K,V], (K,V)](MMap.newBuilder[K,V])
  implicit def buffer[T](implicit buf: Bufferable[T]) = collection[MBuffer[T], T](MBuffer.newBuilder[T])
  implicit def mset[T](implicit buf: Bufferable[T]) = collection[MSet[T], T](MSet.newBuilder[T])

  // TODO we could add IntBuffer/FloatBuffer etc.. to have faster implementations Array[Int]
  implicit def array[T](implicit buf: Bufferable[T], cm: ClassManifest[T]): Bufferable[Array[T]] =
    build[Array[T]] { (bb, a) =>
      // Unfortunately, Array is not traverable, otherwise this is just collection
      val size = a.size
      val nextBb = reallocatingPut(bb){ _.putInt(size) }
      a.foldLeft(nextBb) { (oldbb, t) => reallocatingPut(oldbb) { buf.put(_, t) } }
    } { bb =>
      val builder = Array.newBuilder[T]
      val size = bb.getInt
      builder.clear()
      (0 until size).foreach { idx => builder += buf.get(bb) }
      builder.result()
    }
}
