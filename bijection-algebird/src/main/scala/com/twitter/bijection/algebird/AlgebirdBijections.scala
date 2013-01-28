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

package com.twitter.bijection.algebird

import com.twitter.algebird.{ Group, Monoid, Ring, Semigroup }
import com.twitter.bijection.{ AbstractBijection, Bijection }

import Bijection.asMethod // "as" syntax

/**
 * Bijections on Algebird's abstract algebra datatypes.
 *
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

class BijectedSemigroup[T, U](sg: Semigroup[T])(implicit bij: Bijection[T, U])
extends Semigroup[U] {
  override def plus(l: U, r: U): U = sg.plus(l.as[T], r.as[T]).as[U]
}

class BijectedMonoid[T, U](monoid: Monoid[T])(implicit bij: Bijection[T, U])
extends BijectedSemigroup[T, U](monoid) with Monoid[U] {
  override def zero: U = monoid.zero.as[U]
}

class BijectedGroup[T, U](group: Group[T])(implicit bij: Bijection[T, U])
extends BijectedMonoid[T, U](group) with Group[U] {
  override def negate(u: U): U = group.negate(u.as[T]).as[U]
  override def minus(l: U, r: U): U = group.minus(l.as[T], r.as[T]).as[U]
}

class BijectedRing[T, U](ring: Ring[T])(implicit bij: Bijection[T, U])
extends BijectedGroup[T, U](ring) with Ring[U] {
  override def one: U = ring.one
  override def times(l: U, r: U): U = ring.times(l.as[T], r.as[T]).as[U]
  override def product(iter: TraversableOnce[U]): U =
    ring.product(iter map { _.as[T] }).as[U]
}

trait AlgebirdBijections {
  def semigroupBijection[T, U](implicit bij: Bijection[T, U])
  : Bijection[Semigroup[T], Semigroup[U]] =
    new AbstractBijection[Semigroup[T], Semigroup[U]] {
      override def apply(sg: Semigroup[T]) = new BijectedSemigroup[T, U](sg)
      override def invert(sg: Semigroup[U]) = new BijectedSemigroup[U, T](sg)
    }

  def monoidBijection[T, U](implicit bij: Bijection[T, U])
  : Bijection[Monoid[T], Monoid[U]] =
    new AbstractBijection[Monoid[T], Monoid[U]] {
      override def apply(mon: Monoid[T]) = new BijectedMonoid[T, U](mon)
      override def invert(mon: Monoid[U]) = new BijectedMonoid[U, T](mon)
    }

  def groupBijection[T, U](implicit bij: Bijection[T, U])
  : Bijection[Group[T], Group[U]] =
    new AbstractBijection[Group[T], Group[U]] {
      override def apply(group: Group[T]) = new BijectedGroup[T, U](group)
      override def invert(group: Group[U]) = new BijectedGroup[U, T](group)
    }

  def ringBijection[T, U](implicit bij: Bijection[T, U])
  : Bijection[Ring[T], Ring[U]] =
    new AbstractBijection[Ring[T], Ring[U]] {
      override def apply(ring: Ring[T]) = new BijectedRing[T, U](ring)
      override def invert(ring: Ring[U]) = new BijectedRing[U, T](ring)
    }
}

object AlgebirdBijections extends AlgebirdBijections
