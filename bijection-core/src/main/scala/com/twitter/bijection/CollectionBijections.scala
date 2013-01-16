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

trait CollectionBijections {

  /**
   * Bijections between collection types defined in scala.collection.JavaConverters.
   */
  implicit def iterable2java[T]: Bijection[Iterable[T], JIterable[T]] =
    new Bijection[Iterable[T], JIterable[T]] {
      override def apply(t: Iterable[T]) = t.asJava
      override def invert(t: JIterable[T]) = t.asScala
    }
  implicit def iterator2java[T]: Bijection[Iterator[T], JIterator[T]] =
    new Bijection[Iterator[T], JIterator[T]] {
      override def apply(t: Iterator[T]) = t.asJava
      override def invert(t: JIterator[T]) = t.asScala
    }
  implicit def buffer2java[T]: Bijection[mutable.Buffer[T], JList[T]] =
    new Bijection[mutable.Buffer[T], JList[T]] {
      override def apply(t: mutable.Buffer[T]) = t.asJava
      override def invert(t: JList[T]) = t.asScala
    }
  implicit def mset2java[T]: Bijection[mutable.Set[T], JSet[T]] =
    new Bijection[mutable.Set[T], JSet[T]] {
      override def apply(t: mutable.Set[T]) = t.asJava
      override def invert(t: JSet[T]) = t.asScala
    }
  implicit def mmap2java[K, V]: Bijection[mutable.Map[K,V], JMap[K,V]] =
    new Bijection[mutable.Map[K,V], JMap[K,V]] {
      override def apply(t: mutable.Map[K, V]) = t.asJava
      override def invert(t: JMap[K, V]) = t.asScala
    }
  implicit def concurrentMap2java[K, V]: Bijection[mutable.ConcurrentMap[K, V], JConcurrentMap[K, V]] =
    new Bijection[mutable.ConcurrentMap[K,V], JConcurrentMap[K, V]] {
      override def apply(t: mutable.ConcurrentMap[K, V]) = t.asJava
      override def invert(t: JConcurrentMap[K,V]) = t.asScala
    }
  implicit def iterable2jcollection[T]:  Bijection[Iterable[T], JCollection[T]] =
    new Bijection[Iterable[T], JCollection[T]] {
      override def apply(t: Iterable[T]) = t.asJavaCollection
      override def invert(t: JCollection[T]) = t.asScala
    }
  implicit def iterator2jenumeration[T]: Bijection[Iterator[T], JEnumeration[T]] =
    new Bijection[Iterator[T], JEnumeration[T]] {
      override def apply(t: Iterator[T]) = t.asJavaEnumeration
      override def invert(t: JEnumeration[T]) = t.asScala
    }
  implicit def mmap2jdictionary[K, V]: Bijection[mutable.Map[K, V], JDictionary[K, V]] =
    new Bijection[mutable.Map[K, V], JDictionary[K, V]] {
      override def apply(t: mutable.Map[K, V]) = t.asJavaDictionary
      override def invert(t: JDictionary[K, V]) = t.asScala
    }
  // Here are the immutable ones:
  implicit def seq2Java[T]: Bijection[Seq[T], JList[T]] = new Bijection[Seq[T], JList[T]] {
     def apply(s: Seq[T]) = s.asJava
     override def invert(l: JList[T]) = l.asScala.toSeq
  }
  implicit def set2Java[T]: Bijection[Set[T], JSet[T]] = new Bijection[Set[T], JSet[T]] {
     def apply(s: Set[T]) = s.asJava
     override def invert(l: JSet[T]) = l.asScala.toSet
  }
  implicit def map2Java[K,V]: Bijection[Map[K,V], JMap[K,V]] = new Bijection[Map[K,V], JMap[K,V]] {
     def apply(s: Map[K,V]) = s.asJava
     override def invert(l: JMap[K,V]) = l.asScala.toMap
  }

  /**
   * Accepts a Bijection[A, B] and returns a bijection that can
   * transform traversable containers of A into traversable containers of B.
   *
   * Be careful going from ordered to unordered containers;
   * Bijection[Iterable[A], Set[B]] is inaccurate, and really makes
   * no sense.
   */
  def toContainer[A, B, C <: TraversableOnce[A], D <: TraversableOnce[B]]
  (implicit bij: Bijection[A, B], cd: CanBuildFrom[Nothing, B, D], dc: CanBuildFrom[Nothing, A, C]) =
    new Bijection[C, D] {
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

  implicit def betweenLists[T, U](implicit bij: Bijection[T, U]) = toContainer[T, U, List[T], List[U]]
}
