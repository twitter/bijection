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

import java.net.URL
import java.util.UUID

import scala.annotation.tailrec
import scala.collection.generic.CanBuildFrom

import Bijection.build

trait StringBijections {
  implicit val utf8: Bijection[String, Array[Byte]] = withEncoding("UTF-8")
  def withEncoding(encoding: String): Bijection[String, Array[Byte]] =
    build[String, Array[Byte]] { _.getBytes(encoding) } { new String(_, encoding) }

  // Some bijections with string from standard java/scala classes:
  implicit val url2String: Bijection[URL, String] =
    build[URL, String] { _.toString } { new URL(_) }
  implicit val symbol2String: Bijection[Symbol, String] =
    build[Symbol, String] { _.name } { Symbol(_) }
  implicit val uuid2String: Bijection[UUID, String] =
    build[UUID, String] { _.toString } { UUID.fromString(_) }
  implicit def class2String[T]: Bijection[Class[T], String] =
    CastBijection.of[Class[T], Class[_]] andThen ClassBijection
}

object StringCodec extends StringBijections

/**
 * Bijection for joining together iterables of strings into a single string
 * and splitting them back out. Useful for storing sequences of strings
 * in Config maps.
 */
object StringJoinBijection {
  val DEFAULT_SEP = ":"

  @tailrec
  private def split(str: String, sep: String, acc: List[String] = Nil): List[String] = {
    str.indexOf(sep) match {
      case -1 => (str::acc).reverse
      case idx: Int =>
        split(str.substring(idx + sep.size), sep, str.substring(0, idx) :: acc)
    }
  }

  def apply(separator: String = DEFAULT_SEP): Bijection[Iterable[String], Option[String]] =
    new Bijection[Iterable[String], Option[String]] {
      override def apply(xs: Iterable[String]) = {
        // TODO: Instead of throwing, escape the separator in the encoded string.
        assert(!xs.exists(_.contains(separator)), "Can't encode strings that include the separator.")
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
   * Converts between any collection of A and and Option[String],
   * given an implicit Bijection[A,String].
   */
  def viaContainer[A, B <: TraversableOnce[A]](separator: String = DEFAULT_SEP)
  (implicit bij: Bijection[A, String], ab: CanBuildFrom[Nothing, A, B]): Bijection[B, Option[String]] =
    Bijection.toContainer[A, String, B, Iterable[String]] andThen apply(separator)
}
