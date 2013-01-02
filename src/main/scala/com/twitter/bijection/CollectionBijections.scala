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
    Bijection[Iterable[T], JIterable[T]] { _.asJava } { _.asScala }
  implicit def iterator2java[T]: Bijection[Iterator[T], JIterator[T]] =
    Bijection[Iterator[T], JIterator[T]] { _.asJava } { _.asScala }
  implicit def buffer2java[T]: Bijection[mutable.Buffer[T], JList[T]] =
    Bijection[mutable.Buffer[T], JList[T]] { _.asJava } { _.asScala }
  implicit def set2java[T]: Bijection[mutable.Set[T], JSet[T]] =
    Bijection[mutable.Set[T], JSet[T]] { _.asJava } { _.asScala }
  implicit def map2java[K, V]: Bijection[mutable.Map[K,V], JMap[K,V]] =
    Bijection[mutable.Map[K,V], JMap[K,V]] { _.asJava } { _.asScala }
  implicit def concurrentMap2java[K, V]: Bijection[mutable.ConcurrentMap[K,V], JConcurrentMap[K,V]] =
    Bijection[mutable.ConcurrentMap[K,V], JConcurrentMap[K,V]] { _.asJava } { _.asScala }
  implicit def iterable2jcollection[T]:  Bijection[Iterable[T], JCollection[T]] =
    Bijection[Iterable[T], JCollection[T]] { _.asJavaCollection } { _.asScala }
  implicit def iterator2jenumeration[T]: Bijection[Iterator[T], JEnumeration[T]] =
    Bijection[Iterator[T], JEnumeration[T]] { _.asJavaEnumeration } { _.asScala }
  implicit def map2jdictionary[K, V]: Bijection[mutable.Map[K, V], JDictionary[K, V]] =
    Bijection[mutable.Map[K, V], JDictionary[K, V]] { _.asJavaDictionary } { _.asScala }

  /**
   * Accepts a Bijection[A, B] and returns a bijection that can
   * transform traversable containers of A into traversable containers of B.
   *
   * Be careful going from ordered to unordered containers;
   * Bijection[Iterable[A], Set[B]] is inaccurate, and really makes
   * no sense.
   */
  def toContainer[A, B, C <: TraversableOnce[A], D <: TraversableOnce[B]]
  (implicit bij: Bijection[A, B], cd: CanBuildFrom[C, B, D], dc: CanBuildFrom[D, A, C]) =
    Bijection[C, D] { c: C =>
      val builder = cd(c)
      c foreach { builder += bij(_) }
      builder.result()
    } { d: D =>
      val builder = dc(d)
      d foreach { builder += bij.invert(_) }
      builder.result()
    }
}
