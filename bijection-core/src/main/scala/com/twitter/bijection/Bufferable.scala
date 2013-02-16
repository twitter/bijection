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

import java.io.Serializable
import java.nio.{ByteBuffer, BufferOverflowException}
import java.nio.channels.Channel
import scala.annotation.implicitNotFound
import scala.annotation.tailrec
import scala.collection.mutable.{Builder, Map => MMap, Set => MSet, Buffer => MBuffer}

import scala.util.control.Exception.allCatch

/**
 * Bufferable[T] is a typeclass to work with java.nio.ByteBuffer for serialization/injections to
 * Array[Byte]
 * Always call .duplicate before using the ByteBuffer so the original is not modified
 * (though obviously the backing array is)
 */
@implicitNotFound(msg = "Cannot find Bufferable type class for ${T}")
trait Bufferable[T] extends Serializable {
  def put(into: ByteBuffer, t: T): ByteBuffer
  def get(from: ByteBuffer): Option[(ByteBuffer, T)]
}

/** For Java and avoiding trait bloat
 */
abstract class AbstractBufferable[T] extends Bufferable[T]

/* TODO add a lowest priority where T <: java.io.Serializable
trait LowPriorityBufferable {

}*/

object Bufferable extends GeneratedTupleBufferable with Serializable {
  val DEFAULT_SIZE = 1024
  // To enable: Bufferable.on[Int] syntax
  def on[T](implicit buf: Bufferable[T]): Bufferable[T] = buf
  // Type class methods:
  /** Serialize then deserialize
   */
  def deepCopy[T](t: T)(implicit buf: Bufferable[T]): T = {
    val inj = injectionOf[T]
    inj.invert(inj(t)).get
  }
  def put[T](into: ByteBuffer, t: T)(implicit buf: Bufferable[T]): ByteBuffer = buf.put(into, t)
  def get[T](from: ByteBuffer)(implicit buf: Bufferable[T]): Option[(ByteBuffer, T)] = buf.get(from)
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
  def viaBijection[A,B](implicit buf: Bufferable[B], bij: ImplicitBijection[A,B]): Bufferable[A] =
    Bufferable.build[A] { (bb, a) =>
      buf.put(bb, bij(a))
    } { bb =>
      buf.get(bb).map { tup =>
        (tup._1, bij.invert(tup._2))
      }
    }
  // TODO Bufferable should integrate with injection
  def viaInjection[A,B](implicit buf: Bufferable[B], inj: Injection[A, B]): Bufferable[A] =
    Bufferable.build[A] { (bb, a) =>
      buf.put(bb, inj(a))
    } { bb =>
      for( (rbb, b) <- buf.get(bb); a <- inj.invert(b)) yield (rbb, a)
    }

  def injectionOf[T](implicit buf: Bufferable[T]): Injection[T, Array[Byte]] =
    Injection.build[T, Array[Byte]] { t =>
      getBytes(put(ByteBuffer.allocateDirect(128), t))
    } { bytes => get[T](ByteBuffer.wrap(bytes)).map { _._2 } }

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

  /** remember: putfn and getfn must call duplicate and not change the input ByteBuffer
   * We are duplicating the ByteBuffer state, not the backing array (which IS mutated)
   */
  def build[T](putfn: (ByteBuffer,T) => ByteBuffer)(getfn: (ByteBuffer) => Option[(ByteBuffer, T)]):
    Bufferable[T] = new AbstractBufferable[T] {
      override def put(into: ByteBuffer, t: T) = putfn(into,t)
      override def get(from: ByteBuffer) = getfn(from)
    }
  def buildCatchDuplicate[T](putfn: (ByteBuffer,T) => ByteBuffer)(getfn: (ByteBuffer) => T):
    Bufferable[T] = new AbstractBufferable[T] {
      override def put(into: ByteBuffer, t: T) = putfn(into,t)
      override def get(from: ByteBuffer) = allCatch.opt {
        val dup = from.duplicate
        (dup, getfn(dup))
      }
    }

  // Primitives:
  implicit val byteBufferable: Bufferable[Byte] =
    buildCatchDuplicate[Byte] { (bb, x) => reallocatingPut(bb){ _.put(x) } } { _.get }
  implicit val charBufferable: Bufferable[Char] =
    buildCatchDuplicate[Char] { (bb, x) => reallocatingPut(bb){ _.putChar(x) } } { _.getChar }
  implicit val shortBufferable: Bufferable[Short] =
    buildCatchDuplicate[Short] { (bb, x) => reallocatingPut(bb){ _.putShort(x) } } { _.getShort }
  implicit val intBufferable: Bufferable[Int] =
    buildCatchDuplicate[Int] { (bb, x) => reallocatingPut(bb){ _.putInt(x) } } { _.getInt }
  implicit val longBufferable: Bufferable[Long] =
    buildCatchDuplicate[Long] { (bb, x) => reallocatingPut(bb){ _.putLong(x) } } { _.getLong }
  implicit val floatBufferable: Bufferable[Float] =
    buildCatchDuplicate[Float] { (bb, x) => reallocatingPut(bb){ _.putFloat(x) } }  { _.getFloat }
  implicit val doubleBufferable: Bufferable[Double] =
    buildCatchDuplicate[Double] { (bb, x) => reallocatingPut(bb){ _.putDouble(x) } }  { _.getDouble }
  // Writes a length prefix, and then the bytes
  implicit val byteArray: Bufferable[Array[Byte]] =
    buildCatchDuplicate[Array[Byte]] { (bb, ary) =>
      val nextBb = reallocatingPut(bb){ _.putInt(ary.size) }
      reallocatingPut(nextBb){ _.put(ary) }
    } { bb =>
      val ary = Array.ofDim[Byte](bb.getInt)
      bb.get(ary)
      ary
    }
  implicit val stringBufferable : Bufferable[String] = viaInjection[String, Array[Byte]]
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
      val dup = bb.duplicate
      val byte0 = 0 : Byte
      if(dup.get == byte0) Some((dup, None))
      else {
        buf.get(dup).map { tup => (tup._1, Some(tup._2)) }
      }
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
      val dup = bb.duplicate
      val byte0 = 0 : Byte
      if (dup.get == byte0) {
        bufl.get(dup).map { tup => (tup._1, Left(tup._2)) }
      }
      else {
        bufr.get(dup).map { tup => (tup._1, Right(tup._2)) }
      }
    }

  def putCollection[T](bb: ByteBuffer, l: Traversable[T])(implicit buf: Bufferable[T]): ByteBuffer = {
    val size = l.size
    val nextBb = reallocatingPut(bb){ _.putInt(size) }
    l.foldLeft(nextBb) { (oldbb, t) => reallocatingPut(oldbb) { buf.put(_, t) } }
  }
  def getCollection[T,C](initbb: ByteBuffer, builder: Builder[T,C])(implicit buf: Bufferable[T]):
    Option[(ByteBuffer, C)] = {

    val bbOpt: Option[ByteBuffer] = Some(initbb.duplicate)
    val size = bbOpt.get.getInt
    // We can't mutate the builder while calling other functions (not safe)
    // so we write into this array:
    val ary = new Array[Any](size)
    (0 until size).foldLeft(bbOpt) { (oldBb, idx) =>
      oldBb.flatMap { bb =>
        buf.get(bb) match {
          case None => None
          case Some((newbb, t)) =>
            //Side-effect! scary!!!
            ary(idx) = t
            Some(newbb)
        }
      }
    }
    .map { bb =>
      // Now use the builder:
      builder.clear()
      builder.sizeHint(size)
      ary.foreach { item => builder += item.asInstanceOf[T] }
      (bb, builder.result())
    }
  }
  def collection[C<:Traversable[T],T](builder: Builder[T,C])(implicit buf: Bufferable[T]):
    Bufferable[C] = build[C] { (bb, l) => putCollection(bb, l) }
      { bb => getCollection(bb, builder) }

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
    build[Array[T]] { (bb, l) =>
      putCollection(bb, l.toTraversable)
    } { bb => getCollection(bb, Array.newBuilder[T]) }
}
