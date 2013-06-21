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
import scala.util.control.Exception.allCatch

/**
 * An Injection[A, B] is a function from A to B, and from some B back to A.
 * see: http://mathworld.wolfram.com/Injection.html
 */

@implicitNotFound(msg = "Cannot find Injection type class between ${A} and ${B}")
trait Injection[A, B] extends (A => B) with Serializable { self =>
  def apply(a: A): B
  def invert(b: B): Attempt[A]

  /**
   * Composes two instances of Injection in a new Injection,
   * with this one applied first.
   */
  def andThen[C](g: Injection[B, C]): Injection[A, C] =
    new AbstractInjection[A, C] {
      override def apply(a: A) = g(self.apply(a))
      override def invert(c: C) = g.invert(c).right.flatMap { b => self.invert(b) }
    }
  /** Follow the Injection with a Bijection
   */
  def andThen[C](bij: Bijection[B, C]): Injection[A, C] =
    new AbstractInjection[A, C] {
      override def apply(a: A) = bij(self.apply(a))
      override def invert(c: C) = self.invert(bij.invert(c))
    }

  /**
   * Composes two instances of Injection in a new Injection,
   * with this one applied last.
   */
  def compose[T](g: Injection[T, A]): Injection[T, B] = g andThen this
  def compose[T](bij: Bijection[T, A]): Injection[T, B] =
    new AbstractInjection[T, B] {
      override def apply(t: T) = self.apply(bij(t))
      override def invert(b: B) = self.invert(b).right.map { a => bij.invert(a) }
    }
}

/**
 * Abstract class to ease Bijection creation from Java (and reduce instance
 * size in scala). Prefer to subclass this for anonymous instances.
 */
abstract class AbstractInjection[A, B] extends Injection[A, B] {
  override def apply(a: A): B
  override def invert(b: B): Attempt[A]

  /**
   * This is necessary for interop with Java, which is not smart enough to
   * infer the proper type.
   */
  override def compose[T](g: Function1[T,A]): Function1[T,B] = g andThen this
  override def andThen[T](g: Function1[B,T]): Function1[A,T] = g compose this
}

trait LowPriorityInjections {
  // All Bijections are Injections
  implicit def fromImplicitBijection[A,B](implicit bij: ImplicitBijection[A, B]): Injection[A,B] =
    new AbstractInjection[A, B] {
      override def apply(a: A) = bij(a)
      override def invert(b: B) = Right(bij.invert(b))
    }
}

object Injection extends CollectionInjections
  with Serializable {

  def apply[A, B](a: A)(implicit inj: Injection[A, B]): B = inj(a)
  def invert[A, B](b: B)(implicit inj: Injection[A, B]): Attempt[A] = inj.invert(b)

  def build[A, B](to: A => B)(from: B => Attempt[A]): Injection[A, B] =
    new AbstractInjection[A, B] {
      override def apply(a: A) = to(a)
      override def invert(b: B) = from(b)
    }

  /** Like build, but you give a function from B => A which may throw
   * If you never expect from to throw, use Bijection.build
   */
  def buildCatchInvert[A, B](to: A => B)(from: B => A): Injection[A, B] =
    new AbstractInjection[A, B] {
      override def apply(a: A) = to(a)
      override def invert(b: B) = allCatch.either(from(b))
    }

  /**
   * The "connect" method allows composition of multiple implicit Injections
   * into a single Injection. For example,
   *
   * val composed = connect[Long, Array[Byte], Base64String]: Bijection[Long, Base64String]
   */
  def connect[A, B](implicit bij: Injection[A, B]): Injection[A, B] = bij
  def connect[A, B, C](implicit bij: Injection[A, B], bij2: Injection[B, C]): Injection[A, C] =
    bij andThen bij2
  def connect[A, B, C, D](implicit bij: Injection[A, B], bij2: Injection[B, C], bij3: Injection[C, D]): Injection[A, D] =
    connect[A, B, C] andThen bij3
  def connect[A, B, C, D, E](implicit bij: Injection[A, B], bij2: Injection[B, C], bij3: Injection[C, D], bij4: Injection[D, E]): Injection[A, E] =
    connect[A, B, C, D] andThen bij4

  implicit def attempt[A]: Injection[A, Attempt[A]] =
    new AbstractInjection[A, Attempt[A]] {
      override def apply(a: A) = Right(a)
      override def invert(b: Attempt[A]) = b
    }
  implicit def option[A]: Injection[A, Option[A]] =
    new AbstractInjection[A, Option[A]] {
      override def apply(a: A) = Some(a)
      override def invert(b: Option[A]) = b.toRight(new NoSuchElementException())
    }
  implicit def identity[A]: Injection[A, A] =
    new AbstractInjection[A, A] {
      def apply(a: A) = a
      def invert(a: A) = Right(a)
    }

  implicit def class2String[T]: Injection[Class[T], String] = new ClassInjection[T]

  // Build an injection from a Bijection
  def fromBijection[A,B](bij: Bijection[A, B]): Injection[A,B] =
    new AbstractInjection[A, B] {
      override def apply(a: A) = bij(a)
      override def invert(b: B) = Right(bij.invert(b))
    }
  /*
   * WARNING: this uses java's Class.cast, which is subject to type erasure. If you have
   * a type parameterized type, like List[String] => List[Any], the cast will succeed, but
   * the inner items will not be correct. This is intended for experts.
   */
  implicit def subclass[A, B >: A](implicit cmf: ClassManifest[A]): Injection[A, B] = CastInjection.of[A, B]

  /**
   * Get a partial from B => D from injections and a function from A => C
   */
  def toPartial[A, C, B, D](fn: A => C)(implicit inj1: Injection[A, B], inj2: Injection[C, D]):
    PartialFunction[B, D] = new PartialFunction[B, D] {
      override def isDefinedAt(b: B) = inj1.invert(b).isDefined
      override def apply(b: B): D = inj2.apply(fn(inj1.invert(b).right.get))
    }

  /** Use of this implies you want exceptions when the inverse is undefined
   */
  def unsafeToBijection[A,B](implicit inj: Injection[A,B]): Bijection[A,B] =
    new AbstractBijection[A,B] {
      def apply(a: A) = inj(a)
      override def invert(b: B) = inj.invert(b).right.get
    }
}
