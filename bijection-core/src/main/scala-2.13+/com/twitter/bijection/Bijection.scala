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
import scala.reflect.ClassTag

/**
  * A Bijection[A, B] is a pair of functions that transform an element between
  * types A and B isomorphically; that is, for all items,
  *
  * item == someBijection.inverse(someBijection.apply(item))
  */
@implicitNotFound(msg = "Cannot find Bijection type class between ${A} and ${B}")
trait Bijection[A, B] extends Serializable { self =>
  def apply(a: A): B
  def invert(b: B): A = inverse(b)

  def inverse: Bijection[B, A] =
    new AbstractBijection[B, A] {
      override def apply(b: B) = self.invert(b)
      override def invert(a: A) = self(a)
      override def inverse = self
    }

  /**
    * Composes two instances of Bijection in a new Bijection,
    * with this one applied first.
    */
  def andThen[C](g: Bijection[B, C]): Bijection[A, C] =
    new AbstractBijection[A, C] {
      def apply(a: A) = g(self.apply(a))
      override def invert(c: C) = self.invert(g.invert(c))
    }
  def andThen[C](g: Injection[B, C]): Injection[A, C] = g compose this
  def andThen[C](g: (B => C)): (A => C) = g compose (this.toFunction)

  /**
    * Composes two instances of Bijection in a new Bijection,
    * with this one applied last.
    */
  def compose[T](g: Bijection[T, A]): Bijection[T, B] = new AbstractBijection[T, B] {
    def apply(t: T): B = self(g(t))
    override def invert(b: B): T = g.invert(self.invert(b))
  }
  def compose[T](g: Injection[T, A]): Injection[T, B] = g andThen this
  def compose[T](g: (T => A)): (T => B) = g andThen (this.toFunction)

  def toFunction: (A => B) = new BijectionFn(self)
}

object Bijection extends CollectionBijections with Serializable {

  implicit def toFunction[A, B](bij: Bijection[A, B]): (A => B) = bij.toFunction

  def apply[A, B](a: A)(implicit bij: ImplicitBijection[A, B]): B = bij.bijection(a)
  def invert[A, B](b: B)(implicit bij: ImplicitBijection[A, B]): A = bij.bijection.invert(b)

  def build[A, B](to: A => B)(from: B => A): Bijection[A, B] =
    new AbstractBijection[A, B] {
      override def apply(a: A) = to(a)
      override def invert(b: B) = from(b)
    }

  /**
    * The "connect" method allows composition of multiple implicit bijections
    * into a single bijection. For example,
    *
    * val composed = connect[Long, Array[Byte], Base64String]: Bijection[Long, Base64String]
    */
  def connect[A, B](implicit bij: ImplicitBijection[A, B]): Bijection[A, B] = bij.bijection
  def connect[A, B, C](
      implicit bij: ImplicitBijection[A, B],
      bij2: ImplicitBijection[B, C]
  ): Bijection[A, C] =
    (bij.bijection) andThen (bij2.bijection)
  def connect[A, B, C, D](
      implicit bij1: ImplicitBijection[A, B],
      bij2: ImplicitBijection[B, C],
      bij3: ImplicitBijection[C, D]
  ): Bijection[A, D] =
    connect[A, B, C] andThen (bij3.bijection)
  def connect[A, B, C, D, E](
      implicit bij1: ImplicitBijection[A, B],
      bij2: ImplicitBijection[B, C],
      bij3: ImplicitBijection[C, D],
      bij4: ImplicitBijection[D, E]
  ): Bijection[A, E] =
    connect[A, B, C, D] andThen (bij4.bijection)

  implicit def identity[A]: Bijection[A, A] = new IdentityBijection[A]

  /**
    * We check for default, and return None, else Some
    * Note this never returns Some(default)
    */
  def filterDefault[A](default: A): Bijection[A, Option[A]] =
    new AbstractBijection[A, Option[A]] {
      def apply(a: A) = if (a == default) None else Some(a)
      override def invert(opt: Option[A]) = opt.getOrElse(default)
    }

  /**
    * Converts a function that transforms type A into a function that
    * transforms type B.
    */
  implicit def fnBijection[A, B, C, D](
      implicit bij1: ImplicitBijection[A, B],
      bij2: ImplicitBijection[C, D]
  ): Bijection[A => C, B => D] =
    new AbstractBijection[A => C, B => D] {
      def apply(fn: A => C) = { b =>
        bij2.apply(fn(bij1.invert(b)))
      }
      override def invert(fn: B => D) = { a =>
        bij2.invert(fn(bij1.apply(a)))
      }
    }

  /**
    * Converts a function that combines two arguments of type A into a function that
    * combines two arguments of type B into a single B. Useful for converting
    * input functions to "reduce".
    * TODO: codegen these up to Function22 if they turn out to be useful.
    */
  implicit def fn2Bijection[A, B, C, D, E, F](
      implicit bab: ImplicitBijection[A, B],
      bcd: ImplicitBijection[C, D],
      bef: ImplicitBijection[E, F]
  ): Bijection[(A, C) => E, (B, D) => F] =
    new AbstractBijection[(A, C) => E, (B, D) => F] {
      def apply(fn: (A, C) => E) = { (b, d) =>
        bef.apply(fn(bab.invert(b), bcd.invert(d)))
      }
      override def invert(fn: (B, D) => F) = { (a, c) =>
        bef.invert(fn(bab.apply(a), bcd.apply(c)))
      }
    }

  implicit def swap[T, U]: Bijection[(T, U), (U, T)] = SwapBijection[T, U]

  def subclass[A, B <: A](afn: A => B)(implicit ct: ClassTag[B]): Bijection[A, B] =
    new SubclassBijection[A, B](ct.runtimeClass.asInstanceOf[Class[B]]) {
      def applyfn(a: A) = afn(a)
    }
}
