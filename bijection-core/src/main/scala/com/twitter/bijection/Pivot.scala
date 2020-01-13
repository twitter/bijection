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

object Pivot extends Serializable {

  /**
    * Returns a new Pivot[K, K1, K2] using the supplied bijection
    * to split each input key.
    */
  def apply[K, K1, K2](bijection: Bijection[K, (K1, K2)]): Pivot[K, K1, K2] =
    new PivotImpl(bijection)

  /**
    * Returns a new Pivot[K, K1, K2] using the supplied bijection
    * to split each input key. Bijection can be supplied as an implicit.
    */
  def of[K, K1, K2](implicit impbij: ImplicitBijection[K, (K1, K2)]): Pivot[K, K1, K2] =
    new PivotImpl[K, K1, K2](impbij.bijection)

  /**
    * Returns a new Pivot[(K, V), V, K] -- this Pivot can be used to
    * transform a Map[K,V] -> Map[V,Iterable[K]].
    */
  def swap[K, V]: Pivot[(K, V), V, K] = apply(SwapBijection[K, V])

  def encoder[K, K1, K2](fn: K => (K1, K2)): PivotEncoder[K, K1, K2] =
    new PivotEncoder[K, K1, K2] {
      override val enc = fn
    }
  def decoder[K, K1, K2](fn: ((K1, K2)) => K): PivotDecoder[K, K1, K2] =
    new PivotDecoder[K, K1, K2] {
      override val dec = fn
    }
}

trait PivotEncoder[K, K1, K2] extends (Iterable[K] => Map[K1, Iterable[K2]]) with Serializable {
  def enc: (K) => (K1, K2)

  /**
    * Pivots an Iterable[K] into Map[K1, Iterable[K2]] by
    * pivoting each K into (K1, K2) and grouping all
    * instances of K2 for each K1 into an iterable.
    */
  def apply(pairs: Iterable[K]): Map[K1, Iterable[K2]] =
    pairs
      .map { k =>
        val (k1, k2) = enc(k)
        (k1 -> List(k2))
      }
      .groupBy { _._1 }
      .transform { case (_, v) => v.map { case (_, k2s) => k2s }.flatten }

  /**
    * "Uncurries" the supplied nested fn of K1 and K2  into a function
    * that accepts a single K.
    */
  def unsplit[V](fn: K1 => K2 => V): K => V = { k =>
    val (k1, k2) = enc(k)
    fn(k1)(k2)
  }
}

trait PivotDecoder[K, K1, K2] extends (Map[K1, Iterable[K2]] => Iterable[K]) with Serializable {
  def dec: ((K1, K2)) => K

  /**
    * Expands a Map[K1, Iterable[K2]] into the original
    * Iterable[K] by joining every instance of K2 in each
    * Iterable[K2] with its corresponding instance of K1 and
    * calling `dec` on each pair.
    */
  def apply(m: Map[K1, Iterable[K2]]) = for ((k1, k2s) <- m; k2 <- k2s) yield dec((k1, k2))

  /**
    * Curries the supplied fn of K into a nested function
    * of K1 then K2 using the inversion of `pivot`.
    */
  def split[V](fn: K => V): K1 => K2 => V = {
    k1 =>
      { k2 =>
        fn(dec((k1, k2)))
      }
  }
}

/**
  * Pivot is useful in moving from a 1D space of K to a 2D mapping
  * space of K1 x K2. If the elements within the K space have many repeated
  * elements -- imagine the "time" component of a Key in a timeseries key-value
  * store -- pivoting the changing component into an inner K2 while leaving
  * the repeated component in an outer K1 can assist in compressing
  * a datastore's space requirements.
  *
  * Type Parameters:
  *
  * K: Original Key
  * K1: Outer Key
  * K2: Inner Key
  *
  * Trivial: Pivot[(Event, Timestamp), Event, Timestamp] would
  * pivot the timestamp component out of a compound key.
  *
  *  @author Oscar Boykin
  *  @author Sam Ritchie
  */
trait Pivot[K, K1, K2] extends Bijection[Iterable[K], Map[K1, Iterable[K2]]] {
  def pivot: Bijection[K, (K1, K2)]

  def andThenPivot[K3, K4](after: Bijection[(K1, K2), (K3, K4)]): Pivot[K, K3, K4] =
    Pivot(pivot andThen after)

  def composePivot[T](before: Bijection[T, K]): Pivot[T, K1, K2] =
    Pivot(pivot compose before)

  lazy val encoder = Pivot.encoder[K, K1, K2](pivot.apply _)
  lazy val decoder = Pivot.decoder[K, K1, K2](pivot.invert _)

  override def apply(pairs: Iterable[K]): Map[K1, Iterable[K2]] = encoder(pairs)
  override def invert(m: Map[K1, Iterable[K2]]): Iterable[K] = decoder(m)

  def split[V](fn: K => V): K1 => K2 => V = decoder.split(fn)
  def unsplit[V](fn: K1 => K2 => V): K => V = encoder.unsplit(fn)

  /**
    * Returns a new Pivot that converts an Iterable of (K, T) to an Iterable of
    * ((K1, T), Iterable[K2]). This is useful for applying a new pivoting scheme
    * on top of this one while maintaining some outer key component.
    */
  def wrapOuter[T]: Pivot[(K, T), (K1, T), K2] =
    withValue[T] andThenPivot (new AbstractBijection[(K1, (K2, T)), ((K1, T), K2)] {
      def apply(pair: (K1, (K2, T))) = {
        val (k1, (k2, t)) = pair
        ((k1, t), k2)
      }
      override def invert(pair: ((K1, T), K2)) = {
        val ((k1, t), k2) = pair
        (k1, (k2, t))
      }
    })

  /**
    * Returns a new pivot that converts an Iterable of (K, V) to an Iterable of
    * (K1, Iterable[(K2, V)]). This is useful for pivoting multiple (K, V) pairs into
    * a single key in some KV store.
    */
  def withValue[V]: Pivot[(K, V), K1, (K2, V)] =
    Pivot(new AbstractBijection[(K, V), (K1, (K2, V))] {
      def apply(pair: (K, V)) = {
        val (k, v) = pair
        val (k1, k2) = pivot(k)
        (k1, (k2, v))
      }
      override def invert(pair: (K1, (K2, V))) = {
        val (k1, (k2, v)) = pair
        val k = pivot.invert((k1, k2))
        (k, v)
      }
    })
}

// For use with java, avoiding trait bloat.
class PivotImpl[K, K1, K2](override val pivot: Bijection[K, (K1, K2)]) extends Pivot[K, K1, K2]
