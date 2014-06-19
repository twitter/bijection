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

package com.twitter

import scala.util.Try

/**
 * Bijection trait with numerous implementations.
 *
 * A Bijection[A, B] is an invertible function from A -> B.
 * Knowing that two types have this relationship can be very helpful
 * for serialization (Bijection[T, Array[Byte]]]), communication between
 * libraries (Bijection[MyTrait, YourTrait]) and many other purposes.
 */
package object bijection {

  /**
   * Injections may not be defined for their inverse conversion.
   * This type represents the attempted conversion. A failure
   * will result in a scala.util.Failure containing the InversionFailure. A success
   * will result in a scala.util.Success containing the inverted value.
   *
   * TODO: Remove in 0.6.0.
   */
  type Attempt[T] = Try[T]

  /**
   * Using Injections for serialization is a common pattern. Currying
   * the byte array parameter makes it easier to write code like
   * this:
   *
   * {{{
   * def getProducer[T: Codec] = ...
   * }}}
   */
  type Codec[T] = Injection[T, Array[Byte]]

  /**
   * Tagging infrastructure.
   */
  type Tagged[T] = { type Tag = T }

  /**
   * Tag a type `T` with `Tag`. The resulting type is a subtype of `T`.
   *
   *  The resulting type is used to discriminate between type class instances.
   */
  type @@[T, Tag] = T with Tagged[Tag]

  private[bijection] object Tag {
    @inline def apply[A, T](a: A): A @@ T = a.asInstanceOf[A @@ T]

    def subst[A, F[_], T](fa: F[A]): F[A @@ T] = fa.asInstanceOf[F[A @@ T]]

    def unsubst[A, F[_], T](fa: F[A @@ T]): F[A] = fa.asInstanceOf[F[A]]
  }
}
