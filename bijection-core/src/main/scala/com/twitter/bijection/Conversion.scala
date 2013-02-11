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
/**
 * Convert allows the user to convert an instance of type A to type B given an implicit Conversion
 * that goes between the two.
 *
 * For example, with an implicit Bijection[String,Array[Byte]], the following works:
 * Array(1.toByte, 2.toByte).as[String]
 *
 * Thanks to
 * [hylotech](https://github.com/hylotech/suits/blob/master/src/main/scala/hylotech/util/Bijection.scala)
 * for the following "as" pattern.
 * TODO: this should be a value class in scala 2.10
 */
sealed class Convert[A](a: A) extends Serializable {
  // Not clear to me why this fails on the String @@ Rep[T] pattern, but it seems to:
  // TODO: fix this?
  //def as[B](implicit bij: Bijection[A, _ <: B]): B = bij(a)
  def as[B](implicit conv: Conversion[A, B]): B = conv(a)
  // Syntax to reverse an Injection:
  def asOption[B](implicit inj: Injection[B, A]): Option[B] = inj.invert(a)
}

// Looks like a function, but we don't want a subclass relationship
trait Conversion[A, B] extends Serializable {
  def apply(a: A): B
}

trait CrazyLowPriorityConversion extends Serializable {

}

trait SuperLowPriorityConversion extends CrazyLowPriorityConversion {
  // Due to Bijection.inverseOf, Bijection resolutions diverge if they can't be found.
  // This needs to be lower priority than fromInjection
  implicit def fromBijection[A,B](implicit fn: Bijection[A,B]) = new Conversion[A,B] {
    def apply(a: A) = fn(a)
  }
}

trait LowPriorityConversion extends SuperLowPriorityConversion {
  implicit def fromInjection[A,B](implicit fn: Injection[A,B]) = new Conversion[A,B] {
    def apply(a: A) = fn(a)
  }
}

object Conversion extends LowPriorityConversion {
  implicit def asMethod[A](a: A): Convert[A] = new Convert(a)
  // Both Injection and Bijection subclass (A) => B
  implicit def fromFunction[A,B](implicit fn: Function1[A,B]) = new Conversion[A,B] {
    def apply(a: A) = fn(a)
  }
}

