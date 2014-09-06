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

import java.net.{ URLDecoder, URLEncoder, URL }
import java.util.UUID

import scala.annotation.tailrec
import scala.collection.generic.CanBuildFrom

import com.twitter.bijection.Inversion.attempt
import scala.util.Try

trait StringInjections extends NumericInjections {
  import StringCodec.URLEncodedString

  implicit val utf8: Injection[String, Array[Byte]] = withEncoding("UTF-8")

  def withEncoding(encoding: String): Injection[String, Array[Byte]] =
    new AbstractInjection[String, Array[Byte]] {
      def apply(s: String) = s.getBytes(encoding)
      override def invert(b: Array[Byte]) =
        attempt(b) { new String(_, encoding) }
    }

  // Some bijections with string from standard java/scala classes:
  implicit val url2String: Injection[URL, String] =
    new AbstractInjection[URL, String] {
      def apply(u: URL) = u.toString
      override def invert(s: String) = attempt(s)(new URL(_))
    }

  implicit val uuid2String: Injection[UUID, String] =
    new AbstractInjection[UUID, String] {
      def apply(uuid: UUID) = uuid.toString
      override def invert(s: String) = attempt(s)(UUID.fromString(_))
    }

  implicit val string2UrlEncodedString: Injection[String, URLEncodedString] = new AbstractInjection[String, URLEncodedString] {
    override def apply(a: String): URLEncodedString = URLEncodedString(URLEncoder.encode(a, "UTF-8"))

    override def invert(b: URLEncodedString): Try[String] = attempt(b)(s => URLDecoder.decode(s.encodedString, "UTF-8"))
  }
}

object StringCodec extends StringInjections {
  case class URLEncodedString(encodedString: String) extends AnyVal
}
