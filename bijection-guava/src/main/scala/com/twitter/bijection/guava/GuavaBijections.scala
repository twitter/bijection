/*
 * Copyright 2013 Twitter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.bijection.guava

import com.google.common.base.Optional
import com.twitter.bijection.{AbstractBijection, Bijection, ImplicitBijection, Conversion}
import com.google.common.base.{Function => GFn, Predicate, Supplier}

import Conversion.asMethod

/**
  * Bijections between Scala and Guava.
  */
object GuavaBijections {
  implicit def optional2Option[T, U](
      implicit bij: ImplicitBijection[T, U]): Bijection[Optional[T], Option[U]] =
    Bijection.build[Optional[T], Option[U]] { opt =>
      if (opt.isPresent) Some(bij(opt.get)) else None
    } { opt =>
      if (opt.isDefined) Optional.of[T](bij.invert(opt.get)) else Optional.absent[T]
    }

  /**
    * Converts a scala Function1 into a Guava Function.
    */
  implicit def fn2GuavaFn[A, B, C, D](
      implicit bij1: ImplicitBijection[A, B],
      bij2: ImplicitBijection[C, D]): Bijection[A => C, GFn[B, D]] =
    new AbstractBijection[A => C, GFn[B, D]] {
      def apply(fn: A => C) =
        new GFn[B, D] {
          override def apply(b: B): D = fn(b.as[A]).as[D]
        }
      override def invert(fn: GFn[B, D]) = { a =>
        fn(a.as[B]).as[C]
      }
    }

  /**
    * Converts a scala Function0 into a Guava Supplier.
    */
  implicit def fn2Supplier[T, U](
      implicit bij: ImplicitBijection[T, U]): Bijection[() => T, Supplier[U]] =
    new AbstractBijection[() => T, Supplier[U]] {
      override def apply(fn: () => T) = new Supplier[U] {
        override def get: U = fn.apply.as[U]
      }
      override def invert(supplier: Supplier[U]) = { () =>
        supplier.get.as[T]
      }
    }

  /**
    * Converts a scala Function1[T, Boolean] into a Guava Predicate.
    */
  implicit def fn2Predicate[T, U](
      implicit bij: ImplicitBijection[T, U]): Bijection[T => Boolean, Predicate[U]] =
    new AbstractBijection[T => Boolean, Predicate[U]] {
      override def apply(fn: T => Boolean) = new Predicate[U] {
        override def apply(u: U): Boolean = fn(u.as[T])
      }
      override def invert(pred: Predicate[U]) = { t: T =>
        pred(t.as[U])
      }
    }
}
