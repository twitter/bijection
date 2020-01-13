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

import java.lang.{Iterable => JIterable}
import java.util.{
  Collection => JCollection,
  Dictionary => JDictionary,
  Enumeration => JEnumeration,
  Iterator => JIterator,
  List => JList,
  Map => JMap,
  Set => JSet
}
import scala.jdk.CollectionConverters._
import scala.collection.mutable
import scala.collection.Factory
import scala.reflect.ClassTag

trait CollectionBijections extends BinaryBijections {

  /**
    * Bijections between collection types defined in scala.collection.JavaConverters.
    */
  implicit def iterable2java[T]: Bijection[Iterable[T], JIterable[T]] =
    new AbstractBijection[Iterable[T], JIterable[T]] {
      override def apply(t: Iterable[T]) = t.asJava
      override def invert(u: JIterable[T]) = u.asScala
    }

  implicit def iterator2java[T]: Bijection[Iterator[T], JIterator[T]] =
    new AbstractBijection[Iterator[T], JIterator[T]] {
      override def apply(t: Iterator[T]) = t.asJava
      override def invert(u: JIterator[T]) = u.asScala
    }
  implicit def buffer2java[T]: Bijection[mutable.Buffer[T], JList[T]] =
    new AbstractBijection[mutable.Buffer[T], JList[T]] {
      override def apply(t: mutable.Buffer[T]) = t.asJava
      override def invert(u: JList[T]) = u.asScala
    }
  implicit def mset2java[T]: Bijection[mutable.Set[T], JSet[T]] =
    new AbstractBijection[mutable.Set[T], JSet[T]] {
      override def apply(t: mutable.Set[T]) = t.asJava
      override def invert(u: JSet[T]) = u.asScala
    }
  implicit def mmap2java[K, V]: Bijection[mutable.Map[K, V], JMap[K, V]] =
    new AbstractBijection[mutable.Map[K, V], JMap[K, V]] {
      override def apply(t: mutable.Map[K, V]) = t.asJava
      override def invert(t: JMap[K, V]) = t.asScala
    }
  implicit def iterable2jcollection[T]: Bijection[Iterable[T], JCollection[T]] =
    new AbstractBijection[Iterable[T], JCollection[T]] {
      override def apply(t: Iterable[T]) = t.asJavaCollection
      override def invert(u: JCollection[T]) = u.asScala
    }
  implicit def iterator2jenumeration[T]: Bijection[Iterator[T], JEnumeration[T]] =
    new AbstractBijection[Iterator[T], JEnumeration[T]] {
      override def apply(t: Iterator[T]) = t.asJavaEnumeration
      override def invert(u: JEnumeration[T]) = u.asScala
    }
  implicit def mmap2jdictionary[K, V]: Bijection[mutable.Map[K, V], JDictionary[K, V]] =
    new AbstractBijection[mutable.Map[K, V], JDictionary[K, V]] {
      override def apply(t: mutable.Map[K, V]) = t.asJavaDictionary
      override def invert(t: JDictionary[K, V]) = t.asScala
    }
  // Immutable objects (they copy from java to scala):
  implicit def seq2Java[T]: Bijection[Seq[T], JList[T]] =
    new AbstractBijection[Seq[T], JList[T]] {
      def apply(s: Seq[T]) = s.asJava
      override def invert(l: JList[T]) = l.asScala.toSeq
    }
  implicit def set2Java[T]: Bijection[Set[T], JSet[T]] =
    new AbstractBijection[Set[T], JSet[T]] {
      def apply(s: Set[T]) = s.asJava
      override def invert(l: JSet[T]) = l.asScala.toSet
    }
  implicit def map2Java[K, V]: Bijection[Map[K, V], JMap[K, V]] =
    new AbstractBijection[Map[K, V], JMap[K, V]] {
      def apply(s: Map[K, V]) = s.asJava
      override def invert(l: JMap[K, V]) = l.asScala.toMap
    }

  /*
   * For transformations that may not require a copy, we don't biject on types
   * which would require a copy. To change inner type also, use connect[Seq[T], List[T], List[U]]
   */

  implicit def seq2List[A]: Bijection[Seq[A], List[A]] =
    new AbstractBijection[Seq[A], List[A]] {
      def apply(s: Seq[A]) = s.toList
      override def invert(l: List[A]) = l
    }
  implicit def seq2IndexedSeq[A]: Bijection[Seq[A], IndexedSeq[A]] =
    new AbstractBijection[Seq[A], IndexedSeq[A]] {
      def apply(s: Seq[A]) = s.toIndexedSeq
      override def invert(l: IndexedSeq[A]) = l
    }
  // This doesn't require a copy from Map -> Seq
  implicit def seq2Map[K, V]: Bijection[Seq[(K, V)], Map[K, V]] =
    new AbstractBijection[Seq[(K, V)], Map[K, V]] {
      def apply(s: Seq[(K, V)]) = s.toMap
      override def invert(l: Map[K, V]) = l.toSeq
    }
  // This doesn't require a copy from Set -> Seq
  implicit def seq2Set[T]: Bijection[Seq[T], Set[T]] =
    new AbstractBijection[Seq[T], Set[T]] {
      def apply(s: Seq[T]) = s.toSet
      override def invert(l: Set[T]) = l.toSeq
    }

  protected def trav2Vector[T, C >: Vector[T] <: Iterable[T]]: Bijection[C, Vector[T]] =
    new SubclassBijection[C, Vector[T]](classOf[Vector[T]]) {
      def applyfn(s: C) = {
        // Just build one:
        val bldr = new scala.collection.immutable.VectorBuilder[T]
        bldr ++= s
        bldr.result
      }
    }
  implicit def seq2Vector[T]: Bijection[Seq[T], Vector[T]] = trav2Vector[T, Seq[T]]
  implicit def indexedSeq2Vector[T]: Bijection[IndexedSeq[T], Vector[T]] =
    trav2Vector[T, IndexedSeq[T]]

  /**
    * Accepts a Bijection[A, B] and returns a bijection that can
    * transform iterable containers of A into iterable containers of B.
    *
    * Do not go from ordered to unordered containers;
    * Bijection[Iterable[A], Set[B]] is inaccurate, and really makes
    * no sense.
    */
  def toContainer[A, B, C <: IterableOnce[A], D <: IterableOnce[B]](
      implicit bij: ImplicitBijection[A, B],
      cd: Factory[B, D],
      dc: Factory[A, C]
  ): Bijection[C, D] =
    new AbstractBijection[C, D] {
      def apply(c: C) = {
        val builder = cd.newBuilder
        c.iterator.foreach { builder += bij(_) }
        builder.result()
      }
      override def invert(d: D) = {
        val builder = dc.newBuilder
        d.iterator.foreach { builder += bij.invert(_) }
        builder.result()
      }
    }

  implicit def betweenMaps[K1, V1, K2, V2](
      implicit kBijection: ImplicitBijection[K1, K2],
      vBijection: ImplicitBijection[V1, V2]
  ) =
    toContainer[(K1, V1), (K2, V2), Map[K1, V1], Map[K2, V2]]

  implicit def betweenVectors[T, U](implicit bij: ImplicitBijection[T, U]) =
    toContainer[T, U, Vector[T], Vector[U]]

  implicit def betweenIndexedSeqs[T, U](implicit bij: ImplicitBijection[T, U]) =
    toContainer[T, U, IndexedSeq[T], IndexedSeq[U]]

  implicit def betweenSets[T, U](implicit bij: ImplicitBijection[T, U]) =
    toContainer[T, U, Set[T], Set[U]]

  implicit def betweenSeqs[T, U](implicit bij: ImplicitBijection[T, U]) =
    toContainer[T, U, Seq[T], Seq[U]]

  implicit def betweenLists[T, U](implicit bij: ImplicitBijection[T, U]) =
    toContainer[T, U, List[T], List[U]]

  implicit def option[T, U](
      implicit bij: ImplicitBijection[T, U]
  ): Bijection[Option[T], Option[U]] =
    new AbstractBijection[Option[T], Option[U]] {
      override def apply(optt: Option[T]) = optt.map(bij.bijection)
      override def invert(optu: Option[U]) = optu.map(bij.bijection.inverse)
    }
  // Always requires a copy
  implicit def vector2List[A, B](
      implicit bij: ImplicitBijection[A, B]
  ): Bijection[Vector[A], List[B]] =
    toContainer[A, B, Vector[A], List[B]]

  implicit def indexedSeq2List[A, B](
      implicit bij: ImplicitBijection[A, B]
  ): Bijection[IndexedSeq[A], List[B]] =
    toContainer[A, B, IndexedSeq[A], List[B]]

  /**
    * This doesn't actually copy the Array, only wraps/unwraps with WrappedArray
    */
  implicit def array2Iterable[T: ClassTag]: Bijection[Array[T], Iterable[T]] =
    new AbstractBijection[Array[T], Iterable[T]] {
      override def apply(a: Array[T]) = a.toIterable
      override def invert(t: Iterable[T]) = t.toArray
    }

  /**
    * This doesn't actually copy the Array, only wraps/unwraps with WrappedArray
    */
  implicit def array2Seq[T: ClassTag]: Bijection[Array[T], Seq[T]] =
    new AbstractBijection[Array[T], Seq[T]] {
      override def apply(a: Array[T]) = a.toSeq
      override def invert(t: Seq[T]) = t.toArray
    }
}
