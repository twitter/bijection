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

/**
  * Abstract class to ease Bijection creation from Java.
  */
abstract class AbstractBijection[A, B] extends Bijection[A, B] {
  override def apply(a: A): B
  override def invert(b: B): A
}

trait LowPriorityBijections {

  /**
    * Encoding half of the Cantor–Bernstein–Schroeder theorem
    * http://en.wikipedia.org/wiki/Cantor%E2%80%93Bernstein%E2%80%93Schroeder_theorem
    */
  implicit def fromInjection[A, B](implicit inj: Injection[A, B]): Bijection[A, B @@ Rep[A]] =
    new AbstractBijection[A, B @@ Rep[A]] {
      override def apply(a: A): B @@ Rep[A] = Tag(inj.apply(a))
      // This tag promises the Try will return something:
      override def invert(b: B @@ Rep[A]): A = inj.invert(b).get
    }
}

// Avoid a closure
private[bijection] class BijectionFn[A, B](bij: Bijection[A, B])
    extends (A => B)
    with Serializable {
  def apply(a: A) = bij(a)
}

class IdentityBijection[A] extends Bijection[A, A] {
  override def apply(a: A) = a
  override val inverse = this

  override def andThen[T](g: Bijection[A, T]) = g
  override def compose[T](g: Bijection[T, A]) = g
}

/**
  * When you have conversion between A and B where B is a subclass of A, which is often
  * free, i.e. A is already an instance of A, then this can be faster
  */
abstract class SubclassBijection[A, B <: A](clb: Class[B]) extends Bijection[A, B] {
  protected def applyfn(a: A): B
  def apply(a: A) = {
    if (clb.isAssignableFrom(a.getClass)) {
      // This a is legit:
      a.asInstanceOf[B]
    } else {
      applyfn(a)
    }
  }
  override def invert(b: B): A = b
}

/**
  * Bijection that flips the order of items in a Tuple2.
  */
object SwapBijection {
  def apply[T, U] = new AbstractBijection[(T, U), (U, T)] {
    def apply(t: (T, U)) = t.swap
    override def invert(t: (U, T)) = t.swap
  }
}
