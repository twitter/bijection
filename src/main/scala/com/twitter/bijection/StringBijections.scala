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

trait StringBijections {
  implicit val utf8: Bijection[String, Array[Byte]] = withEncoding("UTF-8")
  def withEncoding(encoding: String): Bijection[String, Array[Byte]] =
    Bijection.build[String, Array[Byte]] { _.getBytes(encoding) } { new String(_, encoding) }
}

object StringCodec extends StringBijections

/**
 * Bijection for joining together iterables of strings into a single string
 * and splitting them back out. Useful for storing sequences of strings
 * in Config maps.
 * Note, the empty Iterable gets round-tripped onto the Iterable of one empty string.
 * There doesn't seem to be a way to easily fix this while keeping the mkString semantics
 * We could return null for an empty iterable, and make an empty list when given a null, but
 * null is very dangerous. Maybe Option[String] is the better idea.
 */
object StringJoinBijection {
  @tailrec
  private def countIn(str: String, substr: String, acc: Int = 0): Int = {
    str.indexOf(substr) match {
      case -1 => acc
      case idx: Int =>
        countIn(str.substring(idx + substr.size), substr, acc + 1)
    }
  }

  def apply(separator: String = ":") =
    Bijection.build[Iterable[String], String] { xs =>
    // TODO: Instead of throwing, escape the separator in the encoded string.
      assert(!xs.exists(_.contains(separator)), "Can't encode strings that include the separator.")
      xs.mkString(separator)
    } { str =>
      // split is not reversible:
      val strings = StringJoinBijection.countIn(str, separator) + 1
      val parts = str.split(separator)
      // Pad out to the right size
      val padSize = strings - parts.size
      if (padSize > 0) {
        parts ++ Array.fill(padSize)("")
      }
      else {
        parts
      }
    }
}
