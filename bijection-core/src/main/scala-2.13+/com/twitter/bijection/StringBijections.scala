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

import scala.annotation.tailrec
import scala.collection.Factory

trait StringBijections extends NumericBijections {
  implicit val symbol2String: Bijection[Symbol, String] =
    new AbstractBijection[Symbol, String] {
      def apply(s: Symbol) = s.name
      override def invert(s: String) = Symbol(s)
    }
}

/**
  * Bijection for joining together iterables of strings into a single string
  * and splitting them back out. Useful for storing sequences of strings
  * in Config maps.
  */
object StringJoinBijection {
  val DEFAULT_SEP = ":"

  @tailrec
  private[bijection] def split(str: String, sep: String, acc: List[String] = Nil): List[String] = {
    str.indexOf(sep) match {
      case -1 => (str :: acc).reverse
      case idx: Int =>
        split(str.substring(idx + sep.size), sep, str.substring(0, idx) :: acc)
    }
  }

  def apply(separator: String = DEFAULT_SEP): Bijection[Iterable[String], Option[String]] =
    new AbstractBijection[Iterable[String], Option[String]] {
      override def apply(xs: Iterable[String]) = {
        // TODO: Instead of throwing, escape the separator in the encoded string.
        assert(
          !xs.exists(_.contains(separator)),
          "Can't encode strings that include the separator."
        )
        if (xs.isEmpty)
          None
        else
          Some(xs.mkString(separator))
      }
      override def invert(strOpt: Option[String]) =
        strOpt match {
          case None => Iterable.empty[String]
          // String#split is not reversible, and uses regexs
          case Some(str) => StringJoinBijection.split(str, separator)
        }
    }

  /**
    * Convert a collection of numbers to and from a string
    * It's common to have types which we know have at least 1 character in their string
    * representation. Knowing that the empty string is not allowed we can map that to the empty
    * collection:
    * TODO add a Tag appoach to Say that N has no zero-length representations
    */
  def nonEmptyValues[N, B <: IterableOnce[N]](
      separator: String = DEFAULT_SEP
  )(
      implicit bij: ImplicitBijection[N, String],
      ab: Factory[N, B]
  ): Bijection[B, String] =
    Bijection
      .toContainer[N, String, B, Iterable[String]]
      .andThen(apply(separator))
      .andThen(Bijection.filterDefault("").inverse)

  /**
    * Converts between any collection of A and and Option[String],
    * given an implicit Bijection[A,String]. To get the final string out,
    * compose with the getOrElse bijection if there is no zero length valid A
    *
    * viaContainer[Int,Set[Int]] andThen Bijection.getOrElse(""): Bijection[Set[Int],String]
    *
    * Note that this can be dangerous with empty collections,
    * as Bijection will try to convert "" -> Int. It's safer to use
    * an instance of type A with the "as" notation for a default item
    * in the collection:
    *
    * viaContainer[Int,Set[Int]] andThen Bijection.getOrElse(0.as[String]): Bijection[Set[Int],String]
    */
  def viaContainer[A, B <: IterableOnce[A]](
      separator: String = DEFAULT_SEP
  )(
      implicit bij: Bijection[A, String],
      ab: Factory[A, B]
  ): Bijection[B, Option[String]] =
    Bijection.toContainer[A, String, B, Iterable[String]] andThen apply(separator)
}
