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
import com.twitter.bijection.Bijection
import com.google.common.base.{ Function => GFn }

/**
 * Bijections between Scala and Guava.
 */

object GuavaBijections {
  implicit def optional2Option[T,U](implicit bij: Bijection[T, U]): Bijection[Optional[T], Option[U]] =
    Bijection.build[Optional[T], Option[U]]
      { opt => if (opt.isPresent) Some(bij(opt.get)) else None }
      { opt => if (opt.isDefined) Optional.of[T](bij.invert(opt.get)) else Optional.absent[T] }

  /**
   * Converts a scala Function1 into a Guava Function.
   */
  implicit def guavaFn2ScalaFn[A, B, C, D](implicit bij1: Bijection[A, B], bij2: Bijection[C, D])
  : Bijection[A => C, GFn[B, D]] =
    new Bijection[A => C, GFn[B, D]] {
      def apply(fn: A => C) =
        new GFn[B, D] {
          override def apply(b: B): D = bij2.apply(fn(bij1.invert(b)))
        }
      override def invert(fn: GFn[B,D]) = { a => bij2.invert(fn.apply(bij1.apply(a))) }
    }
}
