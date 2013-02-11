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

package com.twitter.bijection.clojure

import clojure.lang.{ AFn, IFn }
import com.twitter.bijection.{ AbstractBijection, Bijection, CastInjection }

/**
 * Bijections between Clojure and Scala's types.
 *
 *  @author Sam Ritchie
 */

trait GeneratedIFnBijections {
  // TODO: Extend these to Function20 with macro generation.
  implicit def function0ToIFn[A]: Bijection[Function0[A], IFn] =
    new AbstractBijection[Function0[A], IFn] {
      def apply(fn: Function0[A]) = new AFn {
        override def invoke: AnyRef = fn.apply.asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { () => fn.invoke.asInstanceOf[A] }
    }

  implicit def function1ToIFn[A,B]: Bijection[Function1[A,B], IFn] =
    new AbstractBijection[Function1[A,B], IFn] {
      def apply(fn: Function1[A,B]) = new AFn {
        override def invoke(a: AnyRef): AnyRef = fn.apply(a.asInstanceOf[A]).asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a: A) => fn.invoke(a).asInstanceOf[B] }
    }

  implicit def function2ToIFn[A,B,C]: Bijection[Function2[A,B,C], IFn] =
    new AbstractBijection[Function2[A,B,C], IFn] {
      def apply(fn: Function2[A,B,C]) = new AFn {
        override def invoke(a: AnyRef, b: AnyRef): AnyRef = fn.apply(a.asInstanceOf[A], b.asInstanceOf[B]).asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a: A, b: B) => fn.invoke(a, b).asInstanceOf[C] }
    }
}

trait ClojureBijections extends GeneratedIFnBijections

object ClojureBijections extends ClojureBijections
