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

import java.lang.{ Iterable => JIterable }
import java.util.{
  Collection => JCollection,
  Dictionary => JDictionary,
  Enumeration => JEnumeration,
  Iterator => JIterator,
  List => JList,
  Map => JMap,
  Set => JSet
}
import java.util.concurrent.{ ConcurrentMap => JConcurrentMap }
import scala.collection.JavaConverters._
import scala.collection.mutable
import collection.generic.CanBuildFrom

trait CollectionBijections extends BinaryBijections {
import Bijection.asMethod

  /**
   * Bijections between collection types defined in scala.collection.JavaConverters.
   */
  implicit def iterable2java[T, U](implicit bij: Bijection[T, U]): Bijection[Iterable[T], JIterable[U]] =
    new AbstractBijection[Iterable[T], JIterable[U]] {
      override def apply(t: Iterable[T]) = t.map { _.as[U] }.asJava
      override def invert(u: JIterable[U]) = u.asScala.map { _.as[T] }
    }

  implicit def iterator2java[T, U](implicit bij: Bijection[T, U]): Bijection[Iterator[T], JIterator[U]] =
    new AbstractBijection[Iterator[T], JIterator[U]] {
      override def apply(t: Iterator[T]) = t.map { _.as[U]}.asJava
      override def invert(u: JIterator[U]) = u.asScala.map { _.as[T] }
    }
  implicit def buffer2java[T, U](implicit bij: Bijection[T, U]): Bijection[mutable.Buffer[T], JList[U]] =
    new AbstractBijection[mutable.Buffer[T], JList[U]] {
      override def apply(t: mutable.Buffer[T]) = t.map { _.as[U]}.asJava
      override def invert(u: JList[U]) = u.asScala.map { _.as[T] }
    }
  implicit def mset2java[T, U](implicit bij: Bijection[T, U]): Bijection[mutable.Set[T], JSet[U]] =
    new AbstractBijection[mutable.Set[T], JSet[U]] {
      override def apply(t: mutable.Set[T]) = t.map { _.as[U]}.asJava
      override def invert(u: JSet[U]) = u.asScala.map { _.as[T]}
    }
  implicit def mmap2java[A, B, C, D](implicit keyBij: Bijection[A, C], valBij: Bijection[B, D]): Bijection[mutable.Map[A, B], JMap[C, D]] =
    new AbstractBijection[mutable.Map[A, B], JMap[C, D]] {
      override def apply(t: mutable.Map[A, B]) = t.map { _.as[(C, D)] }.asJava
      override def invert(t: JMap[C, D]) = t.asScala.map { _.as[(A, B)]}
    }
  implicit def concurrentMap2java[K, V]: Bijection[mutable.ConcurrentMap[K, V], JConcurrentMap[K, V]] =
    new AbstractBijection[mutable.ConcurrentMap[K, V], JConcurrentMap[K, V]] {
      override def apply(t: mutable.ConcurrentMap[K, V]) = t.asJava
      override def invert(t: JConcurrentMap[K, V]) = t.asScala
    }
  implicit def iterable2jcollection[T, U](implicit bij: Bijection[T, U]):  Bijection[Iterable[T], JCollection[U]] =
    new AbstractBijection[Iterable[T], JCollection[U]] {
      override def apply(t: Iterable[T]) = t.map { _.as[U] }.asJavaCollection
      override def invert(u: JCollection[U]) = u.asScala.map { _.as[T] }
    }
  implicit def iterator2jenumeration[T, U](implicit bij: Bijection[T, U]): Bijection[Iterator[T], JEnumeration[U]] =
    new AbstractBijection[Iterator[T], JEnumeration[U]] {
      override def apply(t: Iterator[T]) = t.map { _.as[U]}.asJavaEnumeration
      override def invert(u: JEnumeration[U]) = u.asScala.map { _.as[T] }
    }
  implicit def mmap2jdictionary[A, B, C, D](implicit keyBij: Bijection[A, C], valBij: Bijection[B, D]): Bijection[mutable.Map[A, B], JDictionary[C, D]] =
    new AbstractBijection[mutable.Map[A, B], JDictionary[C, D]] {
      override def apply(t: mutable.Map[A, B]) = t.map { _.as[(C, D)] }.asJavaDictionary
      override def invert(t: JDictionary[C, D]) = t.asScala.map { _.as[(A, B)] }
    }
  // Here are the immutable ones:
  implicit def seq2Java[T, U](implicit bij: Bijection[T, U]): Bijection[Seq[T], JList[U]] =
    new AbstractBijection[Seq[T], JList[U]] {
      def apply(s: Seq[T]) = s.map { _.as[U]}.asJava
      override def invert(l: JList[U]) = l.asScala.toSeq.map { _.as[T] }
    }
  implicit def set2Java[T, U](implicit bij: Bijection[T, U]): Bijection[Set[T], JSet[U]] =
    new AbstractBijection[Set[T], JSet[U]] {
      def apply(s: Set[T]) = s.map { _.as[U]}.asJava
      override def invert(l: JSet[U]) = l.asScala.map { _.as[T] }.toSet
    }
  implicit def map2Java[A, B, C, D](implicit keyBij: Bijection[A, C], valBij: Bijection[B, D]): Bijection[Map[A, B], JMap[C, D]] =
    new AbstractBijection[Map[A, B], JMap[C, D]] {
      def apply(s: Map[A, B]) = s.map { _.as[(C, D)]}.asJava
      override def invert(l: JMap[C, D]) = l.asScala.map { _.as[(A, B)] }.toMap
    }

  /**
   * Accepts a Bijection[A, B] and returns a bijection that can
   * transform traversable containers of A into traversable containers of B.
   *
   * Do not go from ordered to unordered containers;
   * Bijection[Iterable[A], Set[B]] is inaccurate, and really makes
   * no sense.
   */
  def toContainer[A, B, C <: TraversableOnce[A], D <: TraversableOnce[B]]
  (implicit bij: Bijection[A, B], cd: CanBuildFrom[Nothing, B, D], dc: CanBuildFrom[Nothing, A, C]): Bijection[C, D] =
    new AbstractBijection[C, D] {
      def apply(c: C) = {
        val builder = cd()
        c foreach { builder += bij(_) }
        builder.result()
      }
      override def invert(d: D) = {
        val builder = dc()
        d foreach { builder += bij.invert(_) }
        builder.result()
      }
    }

  implicit def betweenMaps[K1, V1, K2, V2](implicit kBijection: Bijection[K1, K2], vBijection: Bijection[V1, V2]) =
    toContainer[(K1, V1), (K2, V2), Map[K1, V1], Map[K2, V2]]

  implicit def betweenVectors[T, U](implicit bij: Bijection[T, U]) = toContainer[T, U, Vector[T], Vector[U]]

  implicit def betweenSets[T, U](implicit bij: Bijection[T, U]) = toContainer[T, U, Set[T], Set[U]]

  implicit def betweenLists[T, U](implicit bij: Bijection[T, U]) = toContainer[T, U, List[T], List[U]]

  implicit def option[T, U](implicit bij: Bijection[T, U]): Bijection[Option[T], Option[U]] =
    new AbstractBijection[Option[T], Option[U]] {
      override def apply(optt: Option[T]) = optt.map(bij)
      override def invert(optu: Option[U]) = optu.map(bij.inverse)
    }
  implicit def seq2List[A,B](implicit bij: Bijection[A,B]): Bijection[Seq[A], List[B]]
    = toContainer[A, B, Seq[A], List[B]]
  implicit def vector2List[A,B](implicit bij: Bijection[A,B]): Bijection[Vector[A], List[B]]
    = toContainer[A, B, Vector[A], List[B]]
  implicit def indexedSeq2List[A,B](implicit bij: Bijection[A,B]): Bijection[IndexedSeq[A], List[B]]
    = toContainer[A, B, IndexedSeq[A], List[B]]

  /** This doesn't actually copy the Array, only wraps/unwraps with WrappedArray
   */
  implicit def array2Traversable[T:ClassManifest]: Bijection[Array[T], Traversable[T]] =
    new AbstractBijection[Array[T], Traversable[T]] {
      override def apply(a: Array[T]) = a.toTraversable
      override def invert(t: Traversable[T]) = t.toArray
    }
}
