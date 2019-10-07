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
import scala.util.{Success, Try}

trait CollectionInjections extends StringInjections {

  implicit def optionInjection[A, B](
      implicit inj: Injection[A, B]
  ): Injection[Option[A], Option[B]] =
    new AbstractInjection[Option[A], Option[B]] {
      def apply(a: Option[A]) = a.map(inj)
      def invert(b: Option[B]) = {
        if (b.isEmpty) {
          // This is the inverse of a == None
          Success(None)
        } else {
          val optA = inj.invert(b.get)
          // If it is not None, then convert to Success(Some(_))
          optA.map { Some(_) }
        }
      }
    }
  implicit def option2List[V1, V2](
      implicit inj: Injection[V1, V2]
  ): Injection[Option[V1], List[V2]] =
    new AbstractInjection[Option[V1], List[V2]] {
      def apply(opt: Option[V1]) = opt.map(inj).toList
      def invert(l: List[V2]) = l match {
        case h :: Nil => inj.invert(h).map { Some(_) }
        case Nil      => Success(None)
        case _        => InversionFailure.failedAttempt(l)
      }
    }

  implicit def map2Set[K1, V1, V2](
      implicit inj: Injection[(K1, V1), V2]
  ): Injection[Map[K1, V1], Set[V2]] =
    toContainer[(K1, V1), V2, Map[K1, V1], Set[V2]] { _.size == _.size }

  implicit def set2List[V1, V2](implicit inj: Injection[V1, V2]): Injection[Set[V1], List[V2]] =
    toContainer[V1, V2, Set[V1], List[V2]] { _.size == _.size }

  implicit def set2Vector[V1, V2](implicit inj: Injection[V1, V2]): Injection[Set[V1], Vector[V2]] =
    toContainer[V1, V2, Set[V1], Vector[V2]] { _.size == _.size }

  // This injection is always correct as long as all the items are inverted
  implicit def list2List[V1, V2](implicit inj: Injection[V1, V2]): Injection[List[V1], List[V2]] =
    toContainer[V1, V2, List[V1], List[V2]] { (_, _) =>
      true
    }

  // This injection is always correct as long as all the items are inverted
  implicit def set2Set[V1, V2](implicit inj: Injection[V1, V2]): Injection[Set[V1], Set[V2]] =
    toContainer[V1, V2, Set[V1], Set[V2]] { (_, _) =>
      true
    }

  // This is useful for defining injections, but is too general to be implicit
  def toContainer[A, B, C <: TraversableOnce[A], D <: TraversableOnce[B]](
      goodInv: (D, C) => Boolean
  )(
      implicit inj: Injection[A, B],
      cd: CanBuildFrom[Nothing, B, D],
      dc: CanBuildFrom[Nothing, A, C]
  ): Injection[C, D] =
    new AbstractInjection[C, D] {
      def apply(c: C): D = {
        val builder = cd()
        c foreach { builder += inj(_) }
        builder.result()
      }
      override def invert(d: D): Try[C] = {
        val builder = dc()
        d foreach { b =>
          val thisB = inj.invert(b)
          if (thisB.isSuccess) {
            builder += thisB.get
          } else {
            return InversionFailure.failedAttempt(d)
          }
        }
        val res = builder.result()
        if (goodInv(d, res)) Success(res) else InversionFailure.failedAttempt(d)
      }
    }
}
