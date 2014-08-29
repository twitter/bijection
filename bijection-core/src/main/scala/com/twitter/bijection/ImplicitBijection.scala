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
 * Deals with the type-system issue around resolving implicit bijections.
 * Bijection[A,B] or Bijection[B,A], which should
 * be equivalent. Only use this type as an implicit parameter.
 */
import scala.annotation.implicitNotFound
@implicitNotFound(msg = "Cannot find ImplicitBijection type class from ${A} to ${B}")
sealed trait ImplicitBijection[A, B] extends java.io.Serializable {
  def bijection: Bijection[A, B]
  def apply(a: A) = bijection.apply(a)
  def invert(b: B) = bijection.invert(b)
}
case class Forward[A, B](override val bijection: Bijection[A, B]) extends ImplicitBijection[A, B]
case class Reverse[A, B](inv: Bijection[B, A]) extends ImplicitBijection[A, B] {
  val bijection = inv.inverse
}

trait LowPriorityImplicitBijection extends java.io.Serializable {
  implicit def reverse[A, B](implicit bij: Bijection[B, A]): ImplicitBijection[A, B] = Reverse(bij)
}

object ImplicitBijection extends LowPriorityImplicitBijection {
  implicit def forward[A, B](implicit bij: Bijection[A, B]): ImplicitBijection[A, B] = Forward(bij)
}
