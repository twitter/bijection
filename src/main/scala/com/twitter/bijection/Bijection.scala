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

import scala.annotation.implicitNotFound

/**
 * A Bijection[A, B] is a pair of functions that transform an element between
 * types A and B isomorphically; that is, for all items,
 *
 * item == someBijection.inverse(someBijection.apply(item))
 */

@implicitNotFound(msg = "Cannot find Bijection type class between ${A} and ${B}")
trait Bijection[A, B] extends (A => B) {
  def apply(a: A): B
  def invert(b: B): A = inverse(b)

  def inverse: Bijection[B, A]

  /**
   * Composes two instances of Bijection in a new Bijection,
   * with this one applied first.
   */
  def andThen[C](g: Bijection[B, C]): Bijection[A, C] =
    Bijection[A, C] { a => g(this(a)) } { c => this.invert(g.invert(c)) }

  /**
   * Composes two instances of Bijection in a new Bijection,
   * with this one applied last.
   */
  def compose[T](g: Bijection[T, A]): Bijection[T, B] = g andThen this
}

/**
 * Biject allows the user to convert an instance of type A to type B given an implicit bijection
 * that goes either way between the two.
 *
 * For example, with an implicit Bijection[String,Array[Byte]], the following works:
 * Array(1.toByte, 2.toByte).as[String]
 *
 * Thanks to
 * [hylotech](https://github.com/hylotech/suits/blob/master/src/main/scala/hylotech/util/Bijection.scala)
 * for the following "as" pattern.
 */
sealed class Biject[A](a: A) {
  def as[B](implicit f: Either[Bijection[A, B], Bijection[B, A]]): B = f.fold(_.apply(a), _.invert(a))
}

object Bijection extends NumericBijections with CollectionBijections
  with BinaryBijections with GeneratedTupleBijections {

  def apply[A, B](to: A => B)(from: B => A): Bijection[A, B] =
    new Bijection[A, B] { self =>
      override def apply(a: A) = to(a)
      override val inverse = new Bijection[B, A] {
        override def apply(b: B) = from(b)
        override val inverse = self
      }
    }

  /*
   * Implicit conversion to Biject. This allows the user to use bijections as implicit
   * conversions between types.
   *
   * For example, with an implicit Bijection[String, Array[Byte]], the following works:
   * Array(1.toByte, 2.toByte).as[String]
   */
  implicit def biject[A](a: A): Biject[A] = new Biject(a)
  implicit def forwardEither[A, B](implicit a: Bijection[A, B]): Either[Bijection[A, B], Bijection[B, A]] = Left(a)
  implicit def reverseEither[A, B](implicit b: Bijection[B, A]): Either[Bijection[A, B], Bijection[B, A]] = Right(b)

  implicit def identity[A]: Bijection[A, A] = new IdentityBijection[A]
  implicit def class2String[T]: Bijection[Class[T], String] =
    CastBijection.of[Class[T], Class[_]] andThen ClassBijection

  /**
   * Converts a function that transforms type A into a function that
   * transforms type B.
   */
  implicit def fnBijection[A, B](implicit bij: Bijection[A, B]): Bijection[A => A, B => B] =
    Bijection[A => A, B => B] { fn =>
      { b => bij.apply(fn(bij.invert(b))) }
    } { fn =>
      { a => bij.invert(fn(bij.apply(a))) }
    }

  /**
   * Converts a function that combines two arguments of type A into a function that
   * combines two arguments of type B into a single B. Useful for converting
   * input functions to "reduce".
   */
  implicit def fn2Bijection[A, B](implicit bij: Bijection[A, B]): Bijection[(A, A) => A, (B, B) => B] =
    Bijection[(A, A) => A, (B, B) => B] { fn =>
      { (acc, b) => bij.apply(fn(bij.invert(acc), bij.invert(b))) }
    } { fn =>
      { (acc, a) => bij.invert(fn(bij.apply(acc), bij.apply(a))) }
    }
}

class IdentityBijection[A] extends Bijection[A, A] {
  override def apply(a: A) = a
  override val inverse = this

  override def andThen[T](g: Bijection[A, T]) = g
  override def compose[T](g: Bijection[T, A]) = g
}

/**
 * Bijection that flips the order of items in a Tuple2.
 */
object SwapBijection {
  def apply[T, U] = Bijection[(T, U), (U, T)] { _.swap } { _.swap }
}
