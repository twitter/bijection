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
import scala.annotation.implicitNotFound

/**
 * A Bijection[A, B] is a pair of functions that transform an element between
 * types A and B isomorphically; that is, for all items,
 *
 * item == someBijection.inverse(someBijection.apply(item))
 */

@implicitNotFound(msg = "Cannot find Bijection type class between ${A} and ${B}")
trait Bijection[A, B] extends (A => B) with Serializable { self =>
  def apply(a: A): B
  def invert(b: B): A = inverse(b)

  def inverse: Bijection[B, A] =
    new Bijection[B, A] {
      override def apply(b: B) = self.invert(b)
      override def invert(a: A) = self(a)
    }

  /**
   * Composes two instances of Bijection in a new Bijection,
   * with this one applied first.
   */
  def andThen[C](g: Bijection[B, C]): Bijection[A, C] =
    Bijection.build[A, C] { a => g(this(a)) } { c => this.invert(g.invert(c)) }

  /**
   * Composes two instances of Bijection in a new Bijection,
   * with this one applied last.
   */
  def compose[T](g: Bijection[T, A]): Bijection[T, B] = g andThen this
}

/**
 * Abstract class to ease Bijection creation from Java.
 */
abstract class BijectionImpl[A, B] extends Bijection[A, B] {
  override def apply(a: A): B
  override def invert(b: B): A
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
 * TODO: this should be a value class in scala 2.10
 */
sealed class Biject[A](a: A) {
  def as[B](implicit bij: Bijection[A,B]): B = bij(a)
}

trait LowPriorityBijections {
  implicit def inverseOf[A,B](implicit bij: Bijection[A,B]): Bijection[B,A] = bij.inverse
}

object Bijection extends NumericBijections
  with StringBijections
  with BinaryBijections
  with GeneratedTupleBijections
  with CollectionBijections
  with LowPriorityBijections {

  def apply[A, B](a: A)(implicit bij: Bijection[A, B]): B = bij(a)
  def invert[A, B](b: B)(implicit bij: Bijection[A, B]): A = bij.invert(b)

  def build[A, B](to: A => B)(from: B => A): Bijection[A, B] =
    new Bijection[A, B] { self =>
      override def apply(a: A) = to(a)
      override val inverse = new Bijection[B, A] {
        override def apply(b: B) = from(b)
        override val inverse = self
      }
    }

  /**
   * The "connect" method allows composition of multiple implicit bijections
   * into a single bijection. For example,
   *
   * val composed = connect[Long, Array[Byte], Base64String]: Bijection[Long, Base64String]
   */
  def connect[A, B, C](implicit bij: Bijection[A, B], bij2: Bijection[B, C]): Bijection[A, C] =
    bij andThen bij2
  def connect[A, B, C, D](implicit bij: Bijection[A, B], bij2: Bijection[B, C], bij3: Bijection[C, D]): Bijection[A, D] =
    connect[A, B, C] andThen bij3
  def connect[A, B, C, D, E](implicit bij: Bijection[A, B], bij2: Bijection[B, C], bij3: Bijection[C, D], bij4: Bijection[D, E]): Bijection[A, E] =
    connect[A, B, C, D] andThen bij4

  /*
   * Implicit conversion to Biject. This allows the user to use bijections as implicit
   * conversions between types.
   *
   * For example, with an implicit Bijection[String, Array[Byte]], the following works:
   * Array(1.toByte, 2.toByte).as[String]
   */
  implicit def biject[A](a: A): Biject[A] = new Biject(a)

  implicit def identity[A]: Bijection[A, A] = new IdentityBijection[A]
  /**
   * Converts a function that transforms type A into a function that
   * transforms type B.
   */
  implicit def fnBijection[A, B, C, D](implicit bij1: Bijection[A, B], bij2: Bijection[C, D]):
    Bijection[A => C, B => D] = Bijection.build[A => C, B => D] { fn =>
      { b => bij2.apply(fn(bij1.invert(b))) }
    } { fn =>
      { a => bij2.invert(fn(bij1.apply(a))) }
    }

  /**
   * Converts a function that combines two arguments of type A into a function that
   * combines two arguments of type B into a single B. Useful for converting
   * input functions to "reduce".
   * TODO: codegen these up to Function22 if they turn out to be useful.
   */
  implicit def fn2Bijection[A, B, C, D, E, F]
    (implicit bab: Bijection[A, B], bcd: Bijection[C, D], bef: Bijection[E, F]):
      Bijection[(A, C) => E, (B, D) => F] =
      Bijection.build[(A, C) => E, (B, D) => F] { fn =>
      { (b, d) => bef.apply(fn(bab.invert(b), bcd.invert(d))) }
    } { fn =>
      { (a, c) => bef.invert(fn(bab.apply(a), bcd.apply(c))) }
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
  def apply[T, U] = Bijection.build[(T, U), (U, T)] { _.swap } { _.swap }
}
