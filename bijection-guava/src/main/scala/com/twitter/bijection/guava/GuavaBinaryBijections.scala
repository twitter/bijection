/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.twitter.bijection.guava

import com.twitter.bijection.{AbstractBijection, Bijection}
import com.google.common.io.BaseEncoding

/**
  * @author Muhammad Ashraf
  * @since 7/7/13
  */
object GuavaBinaryBijections {

  trait BaseEncoding extends Any {
    def str: String
  }

  case class Base16String(str: String) extends AnyVal with BaseEncoding

  case class Base32String(str: String) extends AnyVal with BaseEncoding

  case class Base32HEXString(str: String) extends AnyVal with BaseEncoding

  case class Base64String(str: String) extends AnyVal with BaseEncoding

  case class Base64URLString(str: String) extends AnyVal with BaseEncoding

  implicit def unwrap(encodedString: BaseEncoding): String = Option(encodedString) match {
    case Some(x) => x.str
    case None => null
  }

  implicit lazy val bytes2Base64: Bijection[Array[Byte], Base64String] =
    new AbstractBijection[Array[Byte], Base64String] {
      def apply(bytes: Array[Byte]) = Base64String(BaseEncoding.base64().encode(bytes))

      override def invert(b64: Base64String) = BaseEncoding.base64().decode(b64.str)
    }

  implicit lazy val bytes2Base64Url: Bijection[Array[Byte], Base64URLString] =
    new AbstractBijection[Array[Byte], Base64URLString] {
      def apply(bytes: Array[Byte]) = Base64URLString(BaseEncoding.base64Url().encode(bytes))

      override def invert(b64: Base64URLString) = BaseEncoding.base64Url().decode(b64.str)
    }

  implicit lazy val bytes2Base32: Bijection[Array[Byte], Base32String] =
    new AbstractBijection[Array[Byte], Base32String] {
      def apply(bytes: Array[Byte]) = Base32String(BaseEncoding.base32().encode(bytes))

      override def invert(b32: Base32String) = BaseEncoding.base32().decode(b32.str)
    }

  implicit lazy val bytes2Base32HEX: Bijection[Array[Byte], Base32HEXString] =
    new AbstractBijection[Array[Byte], Base32HEXString] {
      def apply(bytes: Array[Byte]) = Base32HEXString(BaseEncoding.base32Hex().encode(bytes))

      override def invert(b32HEX: Base32HEXString) = BaseEncoding.base32Hex().decode(b32HEX.str)
    }

  implicit lazy val bytes2Base16: Bijection[Array[Byte], Base16String] =
    new AbstractBijection[Array[Byte], Base16String] {
      def apply(bytes: Array[Byte]) = Base16String(BaseEncoding.base16().encode(bytes))

      override def invert(b16: Base16String) = BaseEncoding.base16().decode(b16.str)
    }
}
