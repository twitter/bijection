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

object StringCodec {
  def utf8: Bijection[String, Array[Byte]] = withEncoding("UTF-8")
  def withEncoding(encoding: String): Bijection[String, Array[Byte]] =
    Bijection[String, Array[Byte]] { _.getBytes(encoding) } { new String(_, encoding) }
}

/**
 * Bijection for joining together iterables of strings into a single string
 * and splitting them back out. Useful for storing sequences of strings
 * in Config maps.
 */
object StringJoinBijection {
  def apply(separator: String = ":") =
    Bijection[Iterable[String], String] { xs =>
    // TODO: Instead of throwing, escape the separator in the encoded string.
      assert(!xs.exists(_.contains(separator)), "Can't encode strings that include the separator.")
      xs.mkString(separator)
    } { _.split(separator) }
}
