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

import com.twitter.bijection.Inversion.attempt
import java.net.{URLDecoder, URLEncoder, URL}
import java.nio.charset.{Charset, CharsetDecoder, CoderResult, CodingErrorAction}
import java.nio.{ByteBuffer, CharBuffer}
import java.util.UUID
import scala.util.Try

trait StringInjections extends NumericInjections {
  import StringCodec.URLEncodedString

  implicit val utf8: Injection[String, Array[Byte]] = withEncoding("UTF-8")

  def withEncoding(encoding: String): Injection[String, Array[Byte]] =
    new AbstractInjection[String, Array[Byte]] {
      @transient private[this] var decRef: AtomicSharedState[(CharsetDecoder, CharBuffer)] = null

      private[this] def mkSharedState =
        new AtomicSharedState({ () =>
          val dec =
            Charset.forName(encoding).newDecoder.onUnmappableCharacter(CodingErrorAction.REPORT)
          val buf = CharBuffer.allocate(1024) // something big enough, if not big enough, allocate
          (dec, buf)
        })

      def apply(s: String) = s.getBytes(encoding)
      override def invert(b: Array[Byte]) =
        // toString on ByteBuffer is nicer, so use it
        attempt(ByteBuffer.wrap(b)) { bb =>
          //these are mutable, so it can't be shared trivially
          //avoid GC pressure and (probably) perform better
          if (null == decRef) {
            decRef = mkSharedState
          }
          val decBuf = decRef.get
          val dec = decBuf._1
          val buf = decBuf._2
          val maxSpace = (b.length * (dec.maxCharsPerByte.toDouble)).toInt + 1
          val thisBuf = if (maxSpace > buf.limit) CharBuffer.allocate(maxSpace) else buf

          // this is the error free result
          @inline def assertUnderFlow(cr: CoderResult): Unit =
            if (!cr.isUnderflow) cr.throwException

          assertUnderFlow(dec.reset.decode(bb, thisBuf, true))
          assertUnderFlow(dec.flush(thisBuf))
          // set the limit to be the position
          thisBuf.flip
          val str = thisBuf.toString
          // make sure the buffer we store is clear.
          buf.clear
          // we don't reset with the larger buffer to avoid memory leaks
          decRef.release(decBuf)
          str
        }
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

  implicit val string2UrlEncodedString: Injection[String, URLEncodedString] =
    new AbstractInjection[String, URLEncodedString] {
      override def apply(a: String): URLEncodedString =
        URLEncodedString(URLEncoder.encode(a, "UTF-8"))

      override def invert(b: URLEncodedString): Try[String] =
        attempt(b)(s => URLDecoder.decode(s.encodedString, "UTF-8"))
    }
}

object StringCodec extends StringInjections {
  case class URLEncodedString(encodedString: String) extends AnyVal
}
